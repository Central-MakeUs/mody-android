package com.makeus.mody.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.makeus.mody.core.network.interceptor.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션 토큰 암호화 저장소. Android Keystore 로 감싼 MasterKey 로
 * EncryptedSharedPreferences 에 저장 → 디스크 평문 노출/백업 유출 방지.
 *
 * 저장 표현만 암호문이고 [TokenManager] 계약은 그대로라, 재발급/재시도 로직
 * ([com.makeus.mody.core.network.interceptor.TokenAuthenticator])은 영향받지 않는다.
 *
 * 키 무효화(잠금화면 해제 등)나 파일 손상으로 복호화가 실패하면 저장소를 초기화하고
 * 빈 토큰을 반환한다. 빈 refresh 토큰은 TokenAuthenticator 에서 "세션 만료"로 흡수돼
 * 재로그인으로 이어지므로 크래시 없이 복구된다.
 */
@Singleton
class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenManager {

    // 최초 접근 시 생성. 손상 시 reset() 으로 무효화 후 재생성.
    @Volatile
    private var cached: SharedPreferences? = null

    private fun prefs(): SharedPreferences =
        cached ?: synchronized(this) {
            cached ?: create().also { cached = it }
        }

    // 키/파일 손상으로 생성 실패 시 파일 삭제 후 1회 재생성.
    private fun create(): SharedPreferences =
        runCatching { build() }.getOrElse {
            context.deleteSharedPreferences(FILE)
            build()
        }

    private fun build(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    // 개별 값 복호화 실패도 저장소 초기화로 흡수(→ 빈 문자열 = 미로그인 상태).
    private fun read(key: String): String =
        runCatching { prefs().getString(key, null).orEmpty() }
            .getOrElse { reset(); "" }

    private fun write(key: String, value: String) {
        runCatching { prefs().edit().putString(key, value).commit() }
            .onFailure { reset() }
    }

    private fun reset() {
        synchronized(this) {
            runCatching { context.deleteSharedPreferences(FILE) }
            cached = null
        }
    }

    override suspend fun getAccessToken(): String =
        withContext(Dispatchers.IO) { read(KEY_ACCESS) }

    override suspend fun getRefreshToken(): String =
        withContext(Dispatchers.IO) { read(KEY_REFRESH) }

    override suspend fun setAccessToken(accessToken: String) =
        withContext(Dispatchers.IO) { write(KEY_ACCESS, accessToken) }

    override suspend fun setRefreshToken(refreshToken: String) =
        withContext(Dispatchers.IO) { write(KEY_REFRESH, refreshToken) }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            runCatching { prefs().edit().clear().commit() }.onFailure { reset() }
        }
    }

    private companion object {
        const val FILE = "mody_secure_session"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
