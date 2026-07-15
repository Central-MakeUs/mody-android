package com.makeus.mody.core.network.interceptor

import com.makeus.mody.core.network.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 필요한 요청에 Authorization 헤더 주입.
 * 로그인/토큰 관련 무인증 엔드포인트는 [isAccessTokenUsed] 에서 제외.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()

        if (!isAccessTokenUsed(originRequest)) {
            return chain.proceed(originRequest)
        }

        val accessToken = runBlocking { tokenManager.getAccessToken() }
        val request = if (accessToken.isNotBlank()) {
            originRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originRequest
        }
        return chain.proceed(request)
    }

    /** 인증 헤더 없이 호출해야 하는 엔드포인트. 백엔드 경로 확정되면 채울 것. */
    private fun isAccessTokenUsed(request: Request): Boolean {
        // presigned S3 업로드 등 API 호스트가 아닌 요청엔 토큰을 붙이지 않는다 (서명 충돌 방지).
        if (request.url.host != apiHost) return false

        val path = request.url.encodedPath
        val noAuth = path.startsWith("/api/v1/oauth/") ||
            path == "/api/v1/auth/reissue"
        return !noAuth
    }

    private companion object {
        val apiHost: String? = BuildConfig.BASE_URL.toHttpUrlOrNull()?.host
    }
}
