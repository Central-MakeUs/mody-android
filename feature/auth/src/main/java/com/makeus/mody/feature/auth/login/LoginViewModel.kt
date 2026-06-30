package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.feature.auth.login.contract.LoginIntent
import com.makeus.mody.feature.auth.login.contract.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<LoginState, LoginIntent>(LoginState()) {

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            // TODO(auth): 실제 Kakao/Google 인증 미구현. 현재는 화면 전환 테스트용으로
            //  클릭 즉시 온보딩으로 이동. 인증 도입 시 로그인 성공 콜백에서만 navigate 하도록 변경할 것.
            is LoginIntent.KakaoLoginClicked -> navigateToBasicInfo()
            is LoginIntent.GoogleLoginClicked -> navigateToBasicInfo()
        }
    }

    private fun navigateToBasicInfo() {
        navigationHelper.navigate(
            NavigationEvent.To(
                route = OnboardingGraph.HeightWeightInputRoute,
                popUpTo = true,
            )
        )
    }
}
