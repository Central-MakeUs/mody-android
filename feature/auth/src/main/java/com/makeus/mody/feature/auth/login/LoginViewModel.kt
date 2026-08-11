package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.model.error.ErrorAlert
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.domain.repository.SocialLoginProvider
import com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase
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
    private val remoteConfigRepository: RemoteConfigRepository,
    private val resolveStartDestination: ResolveStartDestinationUseCase,
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
        // guest_login_flag 꺼져 있으면(기본값) 히든 진입 자체를 차단.
        // 심사 기간에만 콘솔에서 true 게시 → 스플래시 refresh 로 반영됨.
        if (!remoteConfigRepository.guestLoginEnabled.value) return
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
        // 비밀번호는 원격(Remote Config)에서만 받는다. 상수로 박아두면 APK 디컴파일로 그대로
        // 나오고, 노출되면 앱을 새로 배포해야 바꿀 수 있다.
        // 값이 비어 있으면(미설정·fetch 실패) 통과시키지 않는다 — 빈 입력이 빈 정답과 같아진다.
        val expected = remoteConfigRepository.reviewLoginPassword()
        if (expected.isBlank() || currentState.reviewPassword != expected) {
            setState { copy(reviewPasswordError = true) }
            return
        }
        setState { copy(showReviewLogin = false, reviewPassword = "", isLoading = true, error = null) }
        try {
            authRepository.loginForReview()
            navigateAfterLogin()
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
            authRepository.loginWithSocial(type, socialAccessToken)
            navigateAfterLogin()
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

    /**
     * 로그인 성공 후 진입 화면으로 이동한다.
     *
     * 분기 규칙을 여기서 다시 쓰지 않고 [ResolveStartDestinationUseCase] 를 그대로 태운다.
     * 로그인 응답은 이미 세션에 저장돼 있으므로, 저장된 값을 읽는 이 경로를 쓰면
     * "로그인 직후 진입"과 "앱 재시작 진입"이 규칙 차이로 갈라질 수 없다.
     */
    private suspend fun navigateAfterLogin() {
        val route: Route = when (resolveStartDestination()) {
            StartDestination.ONBOARDING -> OnboardingGraphBaseRoute
            StartDestination.GROUP -> GroupGraphBaseRoute
            StartDestination.MAIN -> MainRoute
            // 로그인 직후인데 미인증 = 토큰 저장이 실패했다. 그대로 보내면 곧장 되튕기므로
            // 이동 대신 에러로 알리고 로그인 화면에 남긴다.
            StartDestination.AUTH -> {
                setState {
                    copy(
                        isLoading = false,
                        error = ErrorAlert(
                            title = "로그인에 실패했어요",
                            message = "로그인 정보를 저장하지 못했어요. 다시 시도해주세요.",
                        ),
                    )
                }
                return
            }
        }
        navigationHelper.navigate(NavigationEvent.To(route, popUpTo = true))
    }

    private companion object {
        /** 심사용 히든 로그인: 로고 연속 탭 목표 횟수 / 허용 간격 */
        const val REVIEW_TAP_TARGET = 20
        const val REVIEW_TAP_WINDOW_MS = 2_000L
    }
}
