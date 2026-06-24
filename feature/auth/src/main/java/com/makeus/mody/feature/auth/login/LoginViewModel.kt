package com.makeus.mody.feature.auth.login

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.feature.auth.login.contract.LoginIntent
import com.makeus.mody.feature.auth.login.contract.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() :
    BaseViewModel<LoginState, LoginIntent>(LoginState()) {

    override suspend fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.KakaoLoginClicked -> Unit // TODO
            is LoginIntent.GoogleLoginClicked -> Unit // TODO
        }
    }
}
