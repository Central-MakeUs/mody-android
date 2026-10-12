package com.makeus.mody.feature.feed.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

sealed interface FeedGraph : Route {
    /**
     * 기록 상세(좌우 슬라이드) + 댓글. 탭한 카드의 groupId/recordId 로 진입.
     * date(ISO): 그날 그룹 전체 기록으로 슬라이드를 구성하기 위한 조회 기준.
     */
    @Serializable
    data class RecordDetailRoute(
        val groupId: Long,
        val recordId: Long,
        val date: String,
    ) : FeedGraph
}
