package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class LoginIntent : UiIntent {
    data object KakaoLoginClicked : LoginIntent()
    data object GoogleLoginClicked : LoginIntent()

    /** 로그인 실패 토스트 표시 완료 → 상태 소비 */
    data object ErrorShown : LoginIntent()
}
