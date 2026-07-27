package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.SocialLoginProvider
import com.makeus.mody.feature.auth.social.SocialLoginCancelledException
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraphBaseRoute
import com.makeus.mody.core.navigation.Route
import com.makeus.mody.feature.auth.login.contract.LoginIntent
import com.makeus.mody.feature.auth.login.contract.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
    private val authRepository: AuthRepository,
    private val socialLoginProvider: SocialLoginProvider,
) : BaseViewModel<LoginState, LoginIntent>(LoginState()) {

    // 심사용 히든 로그인 진입: 로고 연타 카운트(간격 벌어지면 리셋)
    private var logoTapCount = 0
    private var lastLogoTapAtMs = 0L

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.KakaoLoginClicked -> login(SocialLoginType.KAKAO)
            is LoginIntent.GoogleLoginClicked -> login(SocialLoginType.GOOGLE)
            is LoginIntent.ErrorShown -> setState { copy(error = null) }
            is LoginIntent.LogoClicked -> onLogoClicked()
            is LoginIntent.ReviewPasswordChanged ->
                setState { copy(reviewPassword = intent.value, reviewPasswordError = false) }
            is LoginIntent.ReviewLoginSubmitted -> submitReviewLogin()
            is LoginIntent.ReviewDialogDismissed ->
                setState { copy(showReviewLogin = false, reviewPassword = "", reviewPasswordError = false) }
        }
    }

    private fun onLogoClicked() {
        val now = System.currentTimeMillis()
        // 탭 간격이 벌어지면 우연 터치로 보고 처음부터 다시 센다.
        logoTapCount = if (now - lastLogoTapAtMs <= REVIEW_TAP_WINDOW_MS) logoTapCount + 1 else 1
        lastLogoTapAtMs = now
        if (logoTapCount >= REVIEW_TAP_TARGET) {
            logoTapCount = 0
            setState { copy(showReviewLogin = true, reviewPassword = "", reviewPasswordError = false) }
        }
    }

    private suspend fun submitReviewLogin() {
        if (currentState.reviewPassword != REVIEW_PASSWORD) {
            setState { copy(reviewPasswordError = true) }
            return
        }
        setState { copy(showReviewLogin = false, reviewPassword = "", isLoading = true, error = null) }
        try {
            val status = authRepository.loginForReview()
            navigationHelper.navigate(
                NavigationEvent.To(routeAfterLogin(status), popUpTo = true),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.toErrorAlert("로그인에 실패했어요")) }
        }
    }

    private suspend fun login(type: SocialLoginType) {
        setState { copy(isLoading = true, error = null) }
        try {
            val socialAccessToken = socialLoginProvider.getAccessToken(type)
            val status = authRepository.loginWithSocial(type, socialAccessToken)
            navigationHelper.navigate(
                NavigationEvent.To(routeAfterLogin(status), popUpTo = true),
            )
        } catch (e: CancellationException) {
            throw e // 구조적 동시성 유지 — 취소는 전파
        } catch (_: SocialLoginCancelledException) {
            // 사용자가 소셜 로그인 UI를 취소 → 에러 아님, 조용히 종료
            setState { copy(isLoading = false) }
        } catch (e: Exception) {
            // 서버/네트워크/기타 분기는 toErrorAlert 공통 규칙을 따른다.
            setState { copy(isLoading = false, error = e.toErrorAlert("로그인에 실패했어요")) }
        }
    }

    /** 로그인 응답 상태에 따른 진입 화면(ResolveStartDestinationUseCase 와 동일 규칙). */
    private fun routeAfterLogin(status: AuthStatus): Route = when {
        !status.personalInfoCompleted -> OnboardingGraphBaseRoute
        status.mainAccessible -> MainRoute
        else -> GroupGraphBaseRoute
    }

    private companion object {
        /** 심사용 히든 로그인: 로고 연속 탭 목표 횟수 / 허용 간격 / 비밀번호 */
        const val REVIEW_TAP_TARGET = 20
        const val REVIEW_TAP_WINDOW_MS = 2_000L
        const val REVIEW_PASSWORD = "mody2026!"
    }
}
