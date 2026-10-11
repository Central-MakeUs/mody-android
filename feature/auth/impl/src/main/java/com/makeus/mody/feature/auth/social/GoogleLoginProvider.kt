package com.makeus.mody.feature.auth.social

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.makeus.mody.core.commonui.activity.CurrentActivityHolder
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 구글 로그인. Identity Authorization API 로 구글 access token 을 획득한다.
 * 서버가 이 access token 으로 구글 프로필을 조회하므로(ID 토큰 아님) access token 을 반환한다.
 *
 * 최초/미인가 계정은 동의 화면(PendingIntent)이 뜨고 Activity Result 로 결과를 받는다.
 * 이미 인가된 계정은 UI 없이 바로 토큰을 반환한다.
 */
@Singleton
class GoogleLoginProvider @Inject constructor(
    private val activityHolder: CurrentActivityHolder,
) {
    suspend fun login(): String = suspendCancellableCoroutine { continuation ->
        val activity = activityHolder.current as? ComponentActivity
            ?: run {
                continuation.resumeWithException(IllegalStateException("포그라운드 Activity 없음"))
                return@suspendCancellableCoroutine
            }

        val authorizationClient = Identity.getAuthorizationClient(activity)
        val request = createAuthorizationRequest()

        authorizationClient.authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    launchConsent(activity, authorizationClient, result, continuation)
                } else {
                    continuation.resumeWithAccessToken(result)
                }
            }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }

    /**
     * UI 없이 Google access token 획득. 무음 재로그인용.
     *
     * 이미 승인된 계정이면 [AuthorizationClient.authorize] 가 resolution 없이 access token 을 준다.
     * 추가 동의/계정 선택 UI가 필요하면 null 을 반환한다(무음 재로그인 실패 → 로그인 화면 유도).
     */
    suspend fun getAccessTokenSilently(): String? = suspendCancellableCoroutine { continuation ->
        val activity = activityHolder.current as? ComponentActivity
            ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

        Identity.getAuthorizationClient(activity)
            .authorize(createAuthorizationRequest())
            .addOnSuccessListener { result ->
                if (!continuation.isActive) return@addOnSuccessListener
                val token = result.accessToken
                if (!result.hasResolution() && token != null) {
                    continuation.resume(token)
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
    }

    private fun createAuthorizationRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE)))
            .build()

    /** 첫 로그인/미인가 → 동의 화면 실행 후 결과에서 access token 추출. */
    private fun launchConsent(
        activity: ComponentActivity,
        authorizationClient: AuthorizationClient,
        result: AuthorizationResult,
        continuation: CancellableContinuation<String>,
    ) {
        // 이미 취소된 경우 register/launch 하지 않는다.
        // (register 직후 invokeOnCancellation 이 즉시 unregister → launch 시 IllegalStateException 방지)
        if (!continuation.isActive) return

        val pendingIntent = result.pendingIntent
            ?: run {
                continuation.resumeWithException(IllegalStateException("동의 화면 인텐트 없음"))
                return
            }

        // 라이프사이클 비의존 register — 결과 수신 후 즉시 unregister.
        var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
        launcher = activity.activityResultRegistry.register(
            "google_authorize_${System.nanoTime()}",
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { activityResult ->
            launcher?.unregister()
            if (activityResult.resultCode == Activity.RESULT_CANCELED) {
                continuation.resumeWithException(SocialLoginCancelledException())
                return@register
            }
            runCatching { authorizationClient.getAuthorizationResultFromIntent(activityResult.data) }
                .onSuccess { continuation.resumeWithAccessToken(it) }
                .onFailure { continuation.resumeWithException(it) }
        }
        continuation.invokeOnCancellation { launcher?.unregister() }
        launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
    }

    private fun CancellableContinuation<String>.resumeWithAccessToken(result: AuthorizationResult) {
        val token = result.accessToken
        if (token != null) resume(token)
        else resumeWithException(IllegalStateException("구글 access token 없음"))
    }
}
