package com.makeus.mody.feature.onboarding.terms.detail.contract

import com.makeus.mody.core.commonui.base.UiState

/** 약관 전문 WebView 화면 상태. 라우트 인자(type)로 결정된 제목/URL. */
data class TermsDetailState(
    val title: String = "",
    val url: String = "",
) : UiState
