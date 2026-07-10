package com.makeus.mody.feature.auth.social

import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.makeus.mody.core.commonui.activity.CurrentActivityHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Kakao SDK를 사용해 카카오 access token을 획득한다. */
@Singleton
class KakaoLoginProvider @Inject constructor(
    private val activityHolder: CurrentActivityHolder,
) {
    suspend fun login(): String = suspendCancellableCoroutine { continuation ->
        val activity = activityHolder.current
            ?: run {
                continuation.resumeWithException(IllegalStateException("포그라운드 Activity 없음"))
                return@suspendCancellableCoroutine
            }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> continuation.resume(token.accessToken)
                error != null -> continuation.resumeWithException(error)
                else -> continuation.resumeWithException(IllegalStateException("카카오 토큰 없음"))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 사용자가 취소한 경우 중단하고, 그 외 카카오톡 오류는 계정 로그인으로 폴백한다.
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    continuation.resumeWithException(SocialLoginCancelledException())
                } else if (error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }
}
