package com.makeus.mody.feature.onboarding.terms.contract

import com.makeus.mody.core.commonui.base.UiState

/** 필수 약관 동의 화면 상태. 두 필수 약관 모두 체크해야 시작 가능. */
data class TermsState(
    val privacyChecked: Boolean = false,
    val serviceChecked: Boolean = false,
) : UiState {

    /** 전체 동의 여부(= 모든 필수 체크). */
    val allChecked: Boolean
        get() = privacyChecked && serviceChecked

    /** "시작하기" 활성 조건. */
    val canStart: Boolean
        get() = privacyChecked && serviceChecked
}
