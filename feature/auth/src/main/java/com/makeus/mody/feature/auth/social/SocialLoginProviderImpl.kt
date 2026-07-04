package com.makeus.mody.feature.auth.social

import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.makeus.mody.core.commonui.activity.CurrentActivityHolder
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.repository.SocialLoginProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 소셜 SDK 로그인 구현.
 * - Kakao: KakaoTalk 우선, 없거나 실패 시 카카오계정 로그인. accessToken 반환.
 * - Google: TODO (클라이언트ID 확보 후).
 */
@Singleton
class SocialLoginProviderImpl @Inject constructor(
    private val activityHolder: CurrentActivityHolder,
) : SocialLoginProvider {

    override suspend fun getAccessToken(type: SocialLoginType): String = when (type) {
        SocialLoginType.KAKAO -> kakaoLogin()
        SocialLoginType.GOOGLE ->
            throw NotImplementedError("Google 로그인 연동 대기: 클라이언트ID 필요")
    }

    private suspend fun kakaoLogin(): String = suspendCancellableCoroutine { cont ->
        val activity = activityHolder.current
            ?: run {
                cont.resumeWithException(IllegalStateException("포그라운드 Activity 없음"))
                return@suspendCancellableCoroutine
            }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> cont.resume(token.accessToken)
                error != null -> cont.resumeWithException(error)
                else -> cont.resumeWithException(IllegalStateException("카카오 토큰 없음"))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                // 사용자가 카톡 로그인 취소(백버튼)면 중단, 그 외 오류는 계정 로그인으로 폴백
                if (error != null && error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    cont.resumeWithException(error)
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
