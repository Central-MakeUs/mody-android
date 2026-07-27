package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class LoginIntent : UiIntent {
    data object KakaoLoginClicked : LoginIntent()
    data object GoogleLoginClicked : LoginIntent()

    /** 로그인 실패 다이얼로그 확인 → 상태 소비 */
    data object ErrorShown : LoginIntent()

    /** 로고 탭(심사용 히든 로그인 진입 카운트) */
    data object LogoClicked : LoginIntent()
    data class ReviewPasswordChanged(val value: String) : LoginIntent()
    data object ReviewLoginSubmitted : LoginIntent()
    data object ReviewDialogDismissed : LoginIntent()
}
