package com.makeus.mody.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makeus.mody.core.data.cache.NotificationSettingsCache
import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.model.StepSyncCheckpoint
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
        val MAIN_ACCESSIBLE = booleanPreferencesKey("main_accessible")
        val LAST_LOGIN_TYPE = stringPreferencesKey("last_login_type")
        val LAST_GROUP_ID = longPreferencesKey("last_group_id")
        val HEALTH_PERMISSION_ASKED = booleanPreferencesKey("health_permission_asked")

        // 걸음 수 동기화 체크포인트. 세 값이 한 묶음이라 하나라도 없으면 없는 것으로 본다.
        val STEP_SYNC_GROUP_ID = longPreferencesKey("step_sync_group_id")
        val STEP_SYNC_ANCHOR = longPreferencesKey("step_sync_anchor")
        val STEP_SYNC_THROUGH = stringPreferencesKey("step_sync_through")

        /** 더는 읽지 않는 키. 기존 설치에 남은 값을 [clear] 에서 지우기 위해서만 남겨둔다. */
        val LEGACY_GROUP_ONBOARDING_COMPLETED = booleanPreferencesKey("group_onboarding_completed")

        // 콕 찌르기 기록은 날짜 + "groupId:memberId" 집합으로 둔다. 날짜가 바뀌면
        // 집합을 통째로 버리므로 따로 정리하는 로직이 필요 없다.
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
        dataStore.edit { it.write(status) }
    }

    // DataStore 의 edit 는 원자적이고 동시 호출을 직렬화한다 — 읽기와 쓰기를 이 블록 안에
    // 함께 두는 것이 요점. 밖에서 읽어 밖에서 쓰면 그 사이 값이 끼어들어 덮어써진다.
    override suspend fun updateStatus(transform: AuthStatus.() -> AuthStatus) {
        dataStore.edit { it.write(it.readStatus().transform()) }
    }

    override suspend fun getStatus(): AuthStatus =
        safePreferences.map { it.readStatus() }.first()

    private fun Preferences.readStatus(): AuthStatus = AuthStatus(
        personalInfoCompleted = this[Keys.PERSONAL_INFO_COMPLETED] ?: false,
        mainAccessible = this[Keys.MAIN_ACCESSIBLE] ?: false,
    )

    private fun MutablePreferences.write(status: AuthStatus) {
        this[Keys.PERSONAL_INFO_COMPLETED] = status.personalInfoCompleted
        this[Keys.MAIN_ACCESSIBLE] = status.mainAccessible
    }

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


    override suspend fun saveHealthPermissionAsked() {
        dataStore.edit { it[Keys.HEALTH_PERMISSION_ASKED] = true }
    }

    override suspend fun getHealthPermissionAsked(): Boolean =
        safePreferences.map { it[Keys.HEALTH_PERMISSION_ASKED] ?: false }.first()

    // 세 값을 한 edit 안에서 함께 쓴다 — 나눠 쓰면 중간 상태(그룹만 바뀐 체크포인트)가
    // 읽힐 수 있고, 그건 다른 그룹의 진행도를 그대로 이어받는다는 뜻이다.
    override suspend fun saveStepSyncCheckpoint(checkpoint: StepSyncCheckpoint) {
        dataStore.edit {
            it[Keys.STEP_SYNC_GROUP_ID] = checkpoint.groupId
            it[Keys.STEP_SYNC_ANCHOR] = checkpoint.anchorEpochMs
            it[Keys.STEP_SYNC_THROUGH] = checkpoint.syncedThrough
        }
    }

    // 세 값이 함께여야 의미가 있다 — 하나라도 없으면 체크포인트가 없는 것으로 본다.
    override suspend fun getStepSyncCheckpoint(): StepSyncCheckpoint? =
        safePreferences.map { prefs ->
            val groupId = prefs[Keys.STEP_SYNC_GROUP_ID] ?: return@map null
            val anchor = prefs[Keys.STEP_SYNC_ANCHOR] ?: return@map null
            val syncedThrough = prefs[Keys.STEP_SYNC_THROUGH] ?: return@map null
            StepSyncCheckpoint(groupId, anchor, syncedThrough)
        }.first()

    override suspend fun clear() {
        tokenManager.clear()
        dataStore.edit {
            it.remove(Keys.PERSONAL_INFO_COMPLETED)
            it.remove(Keys.MAIN_ACCESSIBLE)
            it.remove(Keys.LEGACY_GROUP_ONBOARDING_COMPLETED)
            it.remove(Keys.LAST_LOGIN_TYPE)
            it.remove(Keys.LAST_GROUP_ID)
            // 계정 전환 시 이전 사용자의 콕 찌르기 이력이 남지 않게 함께 제거.
            // 걸음 수 체크포인트도 마찬가지 — 남겨두면 새 계정의 과거 날짜를 건너뛴다.
            it.remove(Keys.STEP_SYNC_GROUP_ID)
            it.remove(Keys.STEP_SYNC_ANCHOR)
            it.remove(Keys.STEP_SYNC_THROUGH)
        }
        // 계정 전환 시 이전 사용자의 알림 설정 캐시가 노출되지 않게 함께 제거
        // (로그아웃/탈퇴/세션만료 모두 이 clear 를 지나므로 여기서 일괄 처리).
        notificationSettingsCache.clear()
    }
}
