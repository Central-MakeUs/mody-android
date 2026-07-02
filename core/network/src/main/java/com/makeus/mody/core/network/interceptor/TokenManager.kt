package com.makeus.mody.core.network.interceptor

/**
 * 토큰 저장소 추상화. 구현은 :core:data 에서 (현재 in-memory, 추후 DataStore).
 * network 계층은 인터페이스만 알고, 실제 저장 방식은 모른다.
 */
interface TokenManager {
    suspend fun getAccessToken(): String
    suspend fun getRefreshToken(): String
    suspend fun setAccessToken(accessToken: String)
    suspend fun setRefreshToken(refreshToken: String)
    suspend fun clear()
}
