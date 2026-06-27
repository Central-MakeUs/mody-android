package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.AuthGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
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
            is LoginIntent.KakaoLoginClicked -> navigateToBasicInfo()
            is LoginIntent.GoogleLoginClicked -> navigateToBasicInfo()
        }
    }

    private fun navigateToBasicInfo() {
        navigationHelper.navigate(
            NavigationEvent.To(
                route = AuthGraph.BasicInfoRoute,
                popUpTo = true,
            )
        )
    }
}
