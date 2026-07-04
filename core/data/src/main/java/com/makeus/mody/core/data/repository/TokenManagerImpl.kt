package com.makeus.mody.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makeus.mody.core.network.interceptor.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore(Preferences) 기반 토큰 저장소. 앱 재시작 후에도 유지.
 */
@Singleton
class TokenManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TokenManager {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }

    override suspend fun getAccessToken(): String =
        dataStore.data.map { it[Keys.ACCESS].orEmpty() }.first()

    override suspend fun getRefreshToken(): String =
        dataStore.data.map { it[Keys.REFRESH].orEmpty() }.first()

    override suspend fun setAccessToken(accessToken: String) {
        dataStore.edit { it[Keys.ACCESS] = accessToken }
    }

    override suspend fun setRefreshToken(refreshToken: String) {
        dataStore.edit { it[Keys.REFRESH] = refreshToken }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(Keys.ACCESS)
            it.remove(Keys.REFRESH)
        }
    }
}
