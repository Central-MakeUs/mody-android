package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class LoginIntent : UiIntent {
    data object KakaoLoginClicked : LoginIntent()
    data object GoogleLoginClicked : LoginIntent()
}
