package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType
import com.makeus.mody.core.domain.model.error.HttpResponseException
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

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.KakaoLoginClicked -> login(SocialLoginType.KAKAO)
            is LoginIntent.GoogleLoginClicked -> login(SocialLoginType.GOOGLE)
            is LoginIntent.ErrorShown -> setState { copy(errorMessage = null) }
        }
    }

    private suspend fun login(type: SocialLoginType) {
        setState { copy(isLoading = true, errorMessage = null) }
        try {
            val socialAccessToken = socialLoginProvider.getAccessToken(type)
            val status = authRepository.loginWithSocial(type, socialAccessToken)
            navigationHelper.navigate(
                NavigationEvent.To(routeAfterLogin(status), popUpTo = true),
            )
        } catch (e: CancellationException) {
            throw e // 구조적 동시성 유지 — 취소는 전파
        } catch (e: SocialLoginCancelledException) {
            // 사용자가 소셜 로그인 UI를 취소 → 에러 아님, 조용히 종료
            setState { copy(isLoading = false) }
        } catch (e: Exception) {
            // HTTP 예외면 서버 메시지, 그 외(네트워크 등)는 폴백 문구.
            val message = (e as? HttpResponseException)?.msg ?: "로그인에 실패했어요. 다시 시도해주세요."
            setState { copy(isLoading = false, errorMessage = message) }
        }
    }

    /** 로그인 응답 상태에 따른 진입 화면(ResolveStartDestinationUseCase 와 동일 규칙). */
    private fun routeAfterLogin(status: AuthStatus): Route = when {
        !status.personalInfoCompleted -> OnboardingGraphBaseRoute
        status.mainAccessible -> MainRoute
        else -> GroupGraphBaseRoute
    }
}
