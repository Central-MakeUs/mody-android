package com.makeus.mody.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.makeus.mody.core.data.cache.NotificationSettingsCache
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
    private val notificationSettingsCache: NotificationSettingsCache,
) : SessionRepository {

    private object Keys {
        val PERSONAL_INFO_COMPLETED = booleanPreferencesKey("personal_info_completed")
        val GROUP_ONBOARDING_COMPLETED = booleanPreferencesKey("group_onboarding_completed")
        val MAIN_ACCESSIBLE = booleanPreferencesKey("main_accessible")
        val LAST_LOGIN_TYPE = stringPreferencesKey("last_login_type")
        val LAST_GROUP_ID = longPreferencesKey("last_group_id")
        val HEALTH_PERMISSION_ASKED = booleanPreferencesKey("health_permission_asked")

        // 콕 찌르기 기록은 날짜 + "groupId:memberId" 집합으로 둔다. 날짜가 바뀌면
        // 집합을 통째로 버리므로 따로 정리하는 로직이 필요 없다.
        val NUDGED_DATE = stringPreferencesKey("nudged_date")
        val NUDGED_MEMBERS = stringSetPreferencesKey("nudged_members")
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

    override suspend fun saveLastGroupId(groupId: Long) {
        dataStore.edit { it[Keys.LAST_GROUP_ID] = groupId }
    }

    override suspend fun getLastGroupId(): Long? =
        safePreferences.map { it[Keys.LAST_GROUP_ID] }.first()

    override suspend fun saveNudgedMember(groupId: Long, memberId: Long, today: String) {
        dataStore.edit { prefs ->
            // 날짜가 넘어갔으면 이전 기록은 버리고 오늘 것부터 새로 쌓는다.
            val kept = if (prefs[Keys.NUDGED_DATE] == today) {
                prefs[Keys.NUDGED_MEMBERS].orEmpty()
            } else {
                emptySet()
            }
            prefs[Keys.NUDGED_DATE] = today
            prefs[Keys.NUDGED_MEMBERS] = kept + nudgeKey(groupId, memberId)
        }
    }

    override suspend fun getNudgedMembers(groupId: Long, today: String): Set<Long> =
        safePreferences.map { prefs ->
            if (prefs[Keys.NUDGED_DATE] != today) return@map emptySet()
            val prefix = "$groupId:"
            prefs[Keys.NUDGED_MEMBERS].orEmpty()
                .mapNotNull { entry ->
                    entry.removePrefix(prefix).takeIf { it != entry }?.toLongOrNull()
                }
                .toSet()
        }.first()

    private fun nudgeKey(groupId: Long, memberId: Long): String = "$groupId:$memberId"

    override suspend fun saveHealthPermissionAsked() {
        dataStore.edit { it[Keys.HEALTH_PERMISSION_ASKED] = true }
    }

    override suspend fun getHealthPermissionAsked(): Boolean =
        safePreferences.map { it[Keys.HEALTH_PERMISSION_ASKED] ?: false }.first()

    override suspend fun clear() {
        tokenManager.clear()
        dataStore.edit {
            it.remove(Keys.PERSONAL_INFO_COMPLETED)
            it.remove(Keys.GROUP_ONBOARDING_COMPLETED)
            it.remove(Keys.MAIN_ACCESSIBLE)
            it.remove(Keys.LAST_LOGIN_TYPE)
            it.remove(Keys.LAST_GROUP_ID)
            // 계정 전환 시 이전 사용자의 콕 찌르기 이력이 남지 않게 함께 제거.
            it.remove(Keys.NUDGED_DATE)
            it.remove(Keys.NUDGED_MEMBERS)
        }
        // 계정 전환 시 이전 사용자의 알림 설정 캐시가 노출되지 않게 함께 제거
        // (로그아웃/탈퇴/세션만료 모두 이 clear 를 지나므로 여기서 일괄 처리).
        notificationSettingsCache.clear()
    }
}
