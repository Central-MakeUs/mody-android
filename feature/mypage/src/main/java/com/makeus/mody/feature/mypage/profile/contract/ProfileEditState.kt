package com.makeus.mody.feature.mypage.profile.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.LoginType

data class ProfileEditState(
    val avatarUrl: String? = null,
    /** 편집 중인 이름. */
    val name: String = "",
    /** 저장 기준값(변경 여부 판단). */
    val originalName: String = "",
    /** 생년월일(서버 원본, 예: 2002-08-11). 읽기 전용. */
    val birthDate: String? = null,
    val loginType: LoginType = LoginType.UNKNOWN,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    /** 로그아웃/탈퇴 처리 중. */
    val isProcessing: Boolean = false,
    val showWithdrawDialog: Boolean = false,
    /** 탈퇴 API 성공 → 완료 안내. 확인 시 로그인으로 이동. */
    val showWithdrawCompleteDialog: Boolean = false,
    val error: String? = null,
) : UiState {
    /** 최대 글자수 초과(한 글자 더 입력되면 경고). */
    val isNameOverLimit: Boolean get() = name.length > NAME_MAX

    /** 이름이 유효(1~NAME_MAX)하고 원본과 다르면 저장 활성. */
    val isDirty: Boolean
        get() = name.isNotBlank() && !isNameOverLimit && name != originalName

    /** 생년월일 표시용(점 구분). 없으면 빈 문자열. */
    val birthDateDisplay: String get() = birthDate?.replace('-', '.') ?: ""

    companion object {
        /** 이름 최대 글자수(온보딩 닉네임과 동일). */
        const val NAME_MAX = 14
    }
}
