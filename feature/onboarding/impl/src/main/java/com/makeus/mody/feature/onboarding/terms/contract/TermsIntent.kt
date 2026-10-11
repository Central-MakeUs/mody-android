package com.makeus.mody.feature.onboarding.terms.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class TermsIntent : UiIntent {
    /** 전체 동의 토글(현재 전체 동의면 전부 해제, 아니면 전부 체크). */
    data object AllToggled : TermsIntent()

    data object PrivacyToggled : TermsIntent()
    data object ServiceToggled : TermsIntent()

    /** 개인정보처리방침 전문 보기. */
    data object PrivacyDetailClicked : TermsIntent()

    /** 이용약관 전문 보기. */
    data object ServiceDetailClicked : TermsIntent()

    /** 시작하기 → 온보딩 입력(닉네임)으로. */
    data object StartClicked : TermsIntent()
}
