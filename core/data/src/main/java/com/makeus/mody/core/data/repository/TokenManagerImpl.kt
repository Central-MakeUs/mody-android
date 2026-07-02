package com.makeus.mody.core.data.repository

import com.makeus.mody.core.network.interceptor.TokenManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 임시 in-memory 토큰 저장소.
 * TODO: DataStore(:core:datastore) 도입 후 영속 저장으로 교체.
 * 지금은 프로세스 살아있는 동안만 유지 → 앱 재시작 시 소실.
 */
@Singleton
class TokenManagerImpl @Inject constructor() : TokenManager {
    private val lock = Mutex()
    private var accessToken: String = ""
    private var refreshToken: String = ""

    override suspend fun getAccessToken(): String = lock.withLock { accessToken }

    override suspend fun getRefreshToken(): String = lock.withLock { refreshToken }

    override suspend fun setAccessToken(accessToken: String) = lock.withLock {
        this.accessToken = accessToken
    }

    override suspend fun setRefreshToken(refreshToken: String) = lock.withLock {
        this.refreshToken = refreshToken
    }

    override suspend fun clear() = lock.withLock {
        accessToken = ""
        refreshToken = ""
    }
}
