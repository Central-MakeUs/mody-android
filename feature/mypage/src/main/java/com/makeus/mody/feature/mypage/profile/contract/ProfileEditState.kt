package com.makeus.mody.feature.mypage.profile.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.LoginType

data class ProfileEditState(
    /** 서버 원본 프로필 이미지 URL. */
    val avatarUrl: String? = null,
    /** 갤러리에서 고른 로컬 이미지 Uri(미저장). null이면 미선택. */
    val pendingImageUri: String? = null,
    /** "기본 이미지" 선택(서버 기본 아바타로 리셋 예정). */
    val pendingResetDefault: Boolean = false,
    /** 사진 소스 선택 바텀시트 표시 여부. */
    val isPhotoSheetVisible: Boolean = false,
    /** 편집 중인 이름. */
    val name: String = "",
    /** 저장 기준값(변경 여부 판단). */
    val originalName: String = "",
    /** 생년월일(서버 원본, 예: 2002-08-11). 읽기 전용. */
    val birthDate: String? = null,
    val loginType: LoginType = LoginType.UNKNOWN,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** 로그아웃/탈퇴 처리 중. */
    val isProcessing: Boolean = false,
    val showWithdrawDialog: Boolean = false,
    /** 저장 안 된 변경이 있는 채로 나가려 할 때 확인 다이얼로그. */
    val showLeaveDialog: Boolean = false,
    /** 탈퇴 API 성공 → 완료 안내. 확인 시 로그인으로 이동. */
    val showWithdrawCompleteDialog: Boolean = false,
    val error: String? = null,
    /** 에러 다이얼로그 제목. null이면 기본 제목. */
    val errorTitle: String? = null,
) : UiState {
    /** 최대 글자수 초과(한 글자 더 입력되면 경고). */
    val isNameOverLimit: Boolean get() = name.length > NAME_MAX

    /** 표시할 아바타: 선택한 로컬 이미지 > 기본리셋(null) > 서버 원본. */
    val displayAvatarUrl: String?
        get() = pendingImageUri ?: if (pendingResetDefault) null else avatarUrl

    /** 아바타를 변경(갤러리 선택 또는 기본 리셋)했는지. */
    val isAvatarChanged: Boolean get() = pendingImageUri != null || pendingResetDefault

    /** 이름 유효 + (이름 변경 또는 아바타 변경)이면 저장 활성. */
    val isDirty: Boolean
        get() = name.isNotBlank() && !isNameOverLimit && (name != originalName || isAvatarChanged)

    /** 생년월일 표시용(점 구분). 없으면 빈 문자열. */
    val birthDateDisplay: String get() = birthDate?.replace('-', '.') ?: ""

    companion object {
        /** 이름 최대 글자수(온보딩 닉네임과 동일). */
        const val NAME_MAX = 14
    }
}
