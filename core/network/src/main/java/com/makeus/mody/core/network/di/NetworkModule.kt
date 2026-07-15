package com.makeus.mody.core.network.di

import android.util.Log
import com.makeus.mody.core.network.BuildConfig
import com.makeus.mody.core.network.api.AuthApi
import com.makeus.mody.core.network.api.GroupApi
import com.makeus.mody.core.network.api.ModyApi
import com.makeus.mody.core.network.api.OnboardingApi
import com.makeus.mody.core.network.api.RecordApi
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
import javax.inject.Singleton

private const val API_LOG_TAG = "MODY-API"

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
}
