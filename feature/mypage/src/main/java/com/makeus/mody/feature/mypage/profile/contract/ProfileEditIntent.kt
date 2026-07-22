package com.makeus.mody.feature.mypage.profile.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ProfileEditIntent : UiIntent {
    data object Load : ProfileEditIntent()
    data object BackClicked : ProfileEditIntent()

    data class NameChanged(val value: String) : ProfileEditIntent()
    data object SaveClicked : ProfileEditIntent()

    data object LogoutClicked : ProfileEditIntent()

    /** 탈퇴하기 → 확인 다이얼로그 오픈. */
    data object WithdrawClicked : ProfileEditIntent()
    data object WithdrawConfirmed : ProfileEditIntent()
    data object WithdrawDismissed : ProfileEditIntent()

    /** 탈퇴 완료 다이얼로그 확인(스크림/백키 포함) → 로그인으로 이동. */
    data object WithdrawCompleteConfirmed : ProfileEditIntent()

    data object ErrorShown : ProfileEditIntent()
}
