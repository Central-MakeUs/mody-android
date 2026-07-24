package com.makeus.mody.feature.mypage.profile.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ProfileEditIntent : UiIntent {
    data object Load : ProfileEditIntent()
    data object BackClicked : ProfileEditIntent()

    data class NameChanged(val value: String) : ProfileEditIntent()
    data object SaveClicked : ProfileEditIntent()

    /** 아바타 탭 → 사진 소스 시트 오픈. */
    data object AvatarClicked : ProfileEditIntent()
    data object PhotoSheetDismissed : ProfileEditIntent()
    /** 갤러리에서 이미지 선택 완료(로컬 Uri). */
    data class GalleryImageSelected(val uri: String) : ProfileEditIntent()
    /** 기본 이미지로 리셋 선택. */
    data object UseDefaultImageClicked : ProfileEditIntent()

    /** 나가기 확인 다이얼로그: 저장 후 나가기 / 저장 안 하고 나가기 / 취소(머무름). */
    data object LeaveSaveClicked : ProfileEditIntent()
    data object LeaveDiscardClicked : ProfileEditIntent()
    data object LeaveDismissed : ProfileEditIntent()

    data object LogoutClicked : ProfileEditIntent()

    /** 탈퇴하기 → 확인 다이얼로그 오픈. */
    data object WithdrawClicked : ProfileEditIntent()
    data object WithdrawConfirmed : ProfileEditIntent()
    data object WithdrawDismissed : ProfileEditIntent()

    /** 탈퇴 완료 다이얼로그 확인(스크림/백키 포함) → 로그인으로 이동. */
    data object WithdrawCompleteConfirmed : ProfileEditIntent()

    data object ErrorShown : ProfileEditIntent()
}
