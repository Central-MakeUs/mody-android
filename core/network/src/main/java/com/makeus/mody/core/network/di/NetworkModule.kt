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

// API 호출 상한. 대용량 업로드는 이 상한으로는 짧아 PresignedUploader 가 따로 늘려 쓴다.
private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 15L
private const val WRITE_TIMEOUT_SECONDS = 15L
private const val CALL_TIMEOUT_SECONDS = 30L

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
