package com.makeus.mody.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.interceptor.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore + TokenManager 기반 세션 상태 저장소.
 * 로그인 여부는 토큰 존재로, 진행 상태는 flag 로 판단.
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val tokenManager: TokenManager,
) : SessionRepository {

    private object Keys {
        val PERSONAL_INFO_COMPLETED = booleanPreferencesKey("personal_info_completed")
        val GROUP_ONBOARDING_COMPLETED = booleanPreferencesKey("group_onboarding_completed")
        val MAIN_ACCESSIBLE = booleanPreferencesKey("main_accessible")
        val LAST_LOGIN_TYPE = stringPreferencesKey("last_login_type")
    }

    private val safePreferences: Flow<Preferences>
        get() = dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override suspend fun isLoggedIn(): Boolean =
        tokenManager.getAccessToken().isNotBlank()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.setAccessToken(accessToken)
        tokenManager.setRefreshToken(refreshToken)
    }

    override suspend fun getRefreshToken(): String = tokenManager.getRefreshToken()

    override suspend fun saveStatus(status: AuthStatus) {
        dataStore.edit {
            it[Keys.PERSONAL_INFO_COMPLETED] = status.personalInfoCompleted
            it[Keys.GROUP_ONBOARDING_COMPLETED] = status.groupOnboardingCompleted
            it[Keys.MAIN_ACCESSIBLE] = status.mainAccessible
        }
    }

    override suspend fun getStatus(): AuthStatus =
        safePreferences.map {
            AuthStatus(
                personalInfoCompleted = it[Keys.PERSONAL_INFO_COMPLETED] ?: false,
                groupOnboardingCompleted = it[Keys.GROUP_ONBOARDING_COMPLETED] ?: false,
                mainAccessible = it[Keys.MAIN_ACCESSIBLE] ?: false,
            )
        }.first()

    override suspend fun saveLastLoginType(type: SocialLoginType) {
        dataStore.edit { it[Keys.LAST_LOGIN_TYPE] = type.value }
    }

    override suspend fun getLastLoginType(): SocialLoginType? =
        safePreferences.map { prefs ->
            SocialLoginType.entries.firstOrNull { it.value == prefs[Keys.LAST_LOGIN_TYPE] }
        }.first()

    override suspend fun clear() {
        tokenManager.clear()
        dataStore.edit {
            it.remove(Keys.PERSONAL_INFO_COMPLETED)
            it.remove(Keys.GROUP_ONBOARDING_COMPLETED)
            it.remove(Keys.MAIN_ACCESSIBLE)
            it.remove(Keys.LAST_LOGIN_TYPE)
        }
    }
}
