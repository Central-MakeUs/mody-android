package com.makeus.mody.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.makeus.mody.core.domain.repository.PushTokenRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * FCM 토큰을 서버에 동기화. 등록 API 는 인증이 필요하므로 로그인 상태일 때만 호출한다.
 *
 * 호출 시점:
 *  - onNewToken(토큰 생성/갱신)은 서비스가 직접 register 호출.
 *  - 그 외(앱 시작, 로그인 직후)는 여기 [sync] — 이미 발급된 토큰을 조회해 등록.
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val pushTokenRepository: PushTokenRepository,
    private val sessionRepository: SessionRepository,
) {
    // 앱 프로세스 생존 동안 유지되는 IO 스코프. 등록은 fire-and-forget.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 로그인 상태면 현재 FCM 토큰을 서버에 등록. 비로그인/실패는 무음 무시. */
    fun sync() {
        scope.launch {
            if (!sessionRepository.isLoggedIn()) return@launch
            val token = currentToken() ?: return@launch
            runCatching { pushTokenRepository.register(token) }
        }
    }

    /** 로그아웃 시 이 기기 토큰을 서버에서 비활성. */
    fun unregister() {
        scope.launch {
            runCatching { pushTokenRepository.unregister() }
        }
    }

    private suspend fun currentToken(): String? =
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                cont.resume(task.result?.takeIf { task.isSuccessful })
            }
        }
}
