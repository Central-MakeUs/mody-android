package com.makeus.mody.feature.mypage.support.contract

import com.makeus.mody.core.commonui.base.UiState

/** 지원 페이지 WebView 상태. 이용약관·개인정보처리방침·문의 통합 페이지 URL. */
data class SupportState(
    val url: String = "https://mody-support.vercel.app/index.html",
) : UiState
