package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class LoginIntent : UiIntent {
    data object KakaoLoginClicked : LoginIntent()
    data object GoogleLoginClicked : LoginIntent()

    /** 로그인 실패 다이얼로그 확인 → 상태 소비 */
    data object ErrorShown : LoginIntent()
}
