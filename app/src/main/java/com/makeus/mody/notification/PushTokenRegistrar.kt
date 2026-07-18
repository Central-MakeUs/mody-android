package com.makeus.mody.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.makeus.mody.core.domain.notification.PushTokenSynchronizer
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
 * FCM 토큰 서버 동기화의 유일한 진입점. 등록 API 는 인증이 필요하므로 로그인 상태일 때만 호출한다.
 *
 * 호출 시점:
 *  - 앱 시작(ModyApplication.onCreate)
 *  - 로그인 성공 직후(AuthRepositoryImpl → PushTokenSynchronizer)
 *  - 토큰 갱신(ModyFirebaseMessagingService.onNewToken)
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val pushTokenRepository: PushTokenRepository,
    private val sessionRepository: SessionRepository,
) : PushTokenSynchronizer {
    // 앱 프로세스 생존 동안 유지되는 IO 스코프. 등록은 fire-and-forget.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 로그인 상태면 현재 FCM 토큰을 서버에 등록. 비로그인/실패는 무음 무시. */
    override fun sync() {
        scope.launch {
            if (!sessionRepository.isLoggedIn()) return@launch
            val token = currentToken() ?: return@launch
            runCatching { pushTokenRepository.register(token) }
        }
    }

    private suspend fun currentToken(): String? =
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                // 실패 task 에 result 접근하면 RuntimeExecutionException → 먼저 성공 여부 확인.
                cont.resume(if (task.isSuccessful) task.result else null)
            }
        }
}
