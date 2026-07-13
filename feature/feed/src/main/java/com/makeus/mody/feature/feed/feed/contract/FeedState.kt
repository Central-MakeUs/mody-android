package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiState

data class FeedState(
    // 상단 날짜 표기 (예: "7월 18일")
    val dateLabel: String = "",
    // TODO(feed): 피드 목록 API 연동 시 도메인 모델 리스트로 교체
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
) : UiState
