package com.makeus.mody.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

// 토큰 확정 후 DataStore에서 accessToken 주입
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            // TODO: Authorization 헤더 추가
            // .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(request)
    }
}
