package com.makeus.mody.feature.challenge.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

/**
 * 챌린지 관련 풀스크린 route. 챌린지 메인은 바텀탭(MainScreen) 안에 있고,
 * 여기엔 탭 위로 push 되는 화면만 둔다.
 */
sealed interface ChallengeGraph : Route {
    /**
     * 그룹 필수(걸음 수) 챌린지 변경. 챌린지 탭의 "챌린지 변경" 에서 진입.
     * @param groupId 어느 그룹의 챌린지를 바꿀지. 탭이 보고 있는 그룹을 그대로 넘긴다.
     */
    @Serializable
    data class StepChallengeChangeRoute(val groupId: Long) : ChallengeGraph

    /**
     * 주간 챌린지 상세(인증 사진 그리드 + 공유). 챌린지 탭의 주간 챌린지 카드에서 진입.
     *
     * 제목/마감 요일을 인자로 받는다. 상세 API(`GET /weekly-challenges/{challengeId}`)는
     * 목록이 주지 않는 `challengeId` 를 요구해 호출할 수 없어, 목록이 이미 가진 값을 넘긴다.
     * TODO(challenge): 목록 응답에 challengeId 가 추가되면 상세 API 로 대체.
     */
    @Serializable
    data class WeeklyChallengeDetailRoute(
        val groupId: Long,
        val groupChallengeId: Long,
        val title: String,
        /** 서버 enum 문자열(MONDAY~SUNDAY). D-day 표시용. */
        val deadlineDayOfWeek: String,
    ) : ChallengeGraph
}
