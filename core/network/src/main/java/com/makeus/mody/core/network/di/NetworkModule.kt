package com.makeus.mody.core.network.di

import android.util.Log
import com.makeus.mody.core.network.BuildConfig
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.api.ChallengeApi
import com.makeus.mody.core.network.api.FeedApi
import com.makeus.mody.core.network.api.GroupApi
import com.makeus.mody.core.network.api.ModyApi
import com.makeus.mody.core.network.api.NotificationApi
import com.makeus.mody.core.network.api.OnboardingApi
import com.makeus.mody.core.network.api.MyPageApi
import com.makeus.mody.core.network.api.RecordApi
import com.makeus.mody.core.network.api.UploadApi
import com.makeus.mody.core.network.calladapter.ModyCallAdapterFactory
import com.makeus.mody.core.network.interceptor.AuthInterceptor
import com.makeus.mody.core.network.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val API_LOG_TAG = "MODY-API"

// API 호출 상한.
private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 15L
private const val WRITE_TIMEOUT_SECONDS = 15L
private const val CALL_TIMEOUT_SECONDS = 30L

// 업로드 상한. 사진 한 장이 수 MB 라 API 상한으로는 짧다.
private const val UPLOAD_WRITE_TIMEOUT_SECONDS = 60L
private const val UPLOAD_CALL_TIMEOUT_SECONDS = 120L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        // callTimeout 이 없으면 요청 하나가 사실상 무한정 매달릴 수 있다. 특히 TokenAuthenticator 는
        // 재발급을 락 안에서 동기 호출하므로, 그 호출이 안 끝나면 401 을 맞은 다른 요청들이
        // 전부 그 락에 줄 선다. connect/read/write 는 구간별 상한이라 전체 시간을 못 막는다 —
        // 재시도·리다이렉트·인증 재시도까지 포함하는 상한은 callTimeout 뿐이다.
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(
            // 전용 태그로 로깅 → `adb logcat -s MODY-API` 로 API 만 필터.
            // 토큰은 마스킹, debug 에서만 BODY.
            HttpLoggingInterceptor { message -> Log.d(API_LOG_TAG, message) }.apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
                redactHeader("Authorization")
            },
        )
        .build()

    /**
     * presigned URL 업로드 전용 클라이언트. API 클라이언트에서 파생([OkHttpClient.newBuilder])하지
     * 않고 처음부터 따로 만든다 — 파생하면 아래 셋이 그대로 딸려오는데, 업로드에는 전부 해롭다.
     *
     * - **authenticator**: S3 가 401 을 주면 우리 JWT 재발급이 돌고, 실패하면 세션 만료로
     *   번져 사진 업로드 하나가 사용자를 로그아웃시킬 수 있다. S3 401 은 서명 문제이지
     *   우리 세션과 무관하다.
     * - **BODY 로깅**: presigned URL 은 쿼리에 서명이 들어 있어 URL 자체가 비밀이고
     *   (`redactHeader` 는 헤더만 가린다), 이미지 바이트 전체가 로그로 나간다.
     * - **BODY 로깅의 부작용**: 스트리밍 RequestBody 를 미리 버퍼링해 대용량 업로드를
     *   메모리에 올린다 — 스트리밍으로 넘긴 의미가 사라진다.
     *
     * 같은 이유로 공개 이미지를 받는 `ImageShareRepositoryImpl` 도 독립 클라이언트를 쓴다.
     * 커넥션 풀은 공유하지 않는다 — API 호스트와 S3 는 서로 다른 호스트라 재사용될 소켓이 없다.
     */
    @Provides
    @Singleton
    @UploadClient
    fun provideUploadOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        // 업로드는 API 상한으로는 짧다 — 느린 회선에선 사진 한 장도 못 올린다.
        .writeTimeout(UPLOAD_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(UPLOAD_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
        callAdapterFactory: ModyCallAdapterFactory,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .addCallAdapterFactory(callAdapterFactory)
        .build()

    @Provides
    @Singleton
    fun provideModyApi(retrofit: Retrofit): ModyApi =
        retrofit.create(ModyApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideOnboardingApi(retrofit: Retrofit): OnboardingApi =
        retrofit.create(OnboardingApi::class.java)

    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi =
        retrofit.create(GroupApi::class.java)

    @Provides
    @Singleton
    fun provideRecordApi(retrofit: Retrofit): RecordApi =
        retrofit.create(RecordApi::class.java)

    @Provides
    @Singleton
    fun provideFeedApi(retrofit: Retrofit): FeedApi =
        retrofit.create(FeedApi::class.java)

    @Provides
    @Singleton
    fun provideChallengeApi(retrofit: Retrofit): ChallengeApi =
        retrofit.create(ChallengeApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageApi =
        retrofit.create(MyPageApi::class.java)

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi =
        retrofit.create(UploadApi::class.java)
}
