package com.makeus.mody.feature.mypage.profile.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ProfileEditIntent : UiIntent {
    data object Load : ProfileEditIntent()
    data object BackClicked : ProfileEditIntent()

    data class NameChanged(val value: String) : ProfileEditIntent()
    data object SaveClicked : ProfileEditIntent()

    /** 나가기 확인 다이얼로그: 저장 후 나가기 / 저장 안 하고 나가기 / 취소(머무름). */
    data object LeaveSaveClicked : ProfileEditIntent()
    data object LeaveDiscardClicked : ProfileEditIntent()
    data object LeaveDismissed : ProfileEditIntent()

    data object LogoutClicked : ProfileEditIntent()

    /** 탈퇴하기 → 확인 다이얼로그 오픈. */
    data object WithdrawClicked : ProfileEditIntent()
    data object WithdrawConfirmed : ProfileEditIntent()
    data object WithdrawDismissed : ProfileEditIntent()

    data object ErrorShown : ProfileEditIntent()
}
