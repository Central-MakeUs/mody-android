package com.makeus.mody.core.domain.model

/**
 * 기록 상세. 탭한 기록부터 좌우로 넘겨보는 슬라이드.
 * records 는 커서로 이어붙이며, currentIndex 가 진입 시 위치.
 */
data class RecordDetail(
    val totalCount: Int,
    val currentIndex: Int,
    val records: List<FeedRecord>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
