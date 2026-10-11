package com.makeus.mody.feature.group.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object GroupGraphBaseRoute : Route

/**
 * 그룹 코드 참여 화면([GroupGraph.GroupEntryRoute]) 진입 출처.
 * 화면 문구(title/subtitle)와 뒤로가기 노출을 소스별로 분기한다.
 * 새 진입 경로(예: 전체 그룹 탈퇴 후 재참여)가 생기면 여기에 추가한다.
 */
enum class GroupEntrySource {
    /** 온보딩 회원가입 완료 플로우 — 시작점이라 뒤로가기 없음. */
    Onboarding,

    /** 피드 "그룹 추가하기 → 참여" — 피드로 복귀하는 뒤로가기 있음. */
    Feed,

    /** 전체 그룹 탈퇴 후 강제 재참여 — 돌아갈 곳이 없어 뒤로가기 없음. */
    NoGroup,
}

sealed interface GroupGraph : Route {
    /**
     * 그룹 코드 참여 화면.
     * @param source 진입 출처. 소스별로 title/subtitle/뒤로가기 노출을 분기한다.
     *               (온보딩=플로우 시작점이라 뒤로가기 없음, 피드=복귀용 뒤로가기 있음 등)
     */
    @Serializable
    data class GroupEntryRoute(
        val source: GroupEntrySource = GroupEntrySource.Onboarding,
    ) : GroupGraph

    @Serializable
    data object JoinGroupRoute : GroupGraph

    @Serializable
    data object CreateGroupRoute : GroupGraph

    @Serializable
    data object GroupShareRoute : GroupGraph
}
