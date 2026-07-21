package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
data object AuthGraphBaseRoute : Route

sealed interface AuthGraph : Route {
    @Serializable
    data object LoginRoute : AuthGraph
}

@Serializable
data object OnboardingGraphBaseRoute : Route

sealed interface OnboardingGraph : Route {
    // 입력 스텝 순서: 닉네임 → 생년월일 → 체중 → 알림 → 완료
    @Serializable
    data object NicknameRoute : OnboardingGraph

    @Serializable
    data object BirthRoute : OnboardingGraph

    @Serializable
    data object WeightRoute : OnboardingGraph

    @Serializable
    data object AlarmRoute : OnboardingGraph
}

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

@Serializable
data object RecordGraphBaseRoute : Route

sealed interface RecordGraph : Route {
    /** 식사 기록 (record/food) */
    @Serializable
    data object FoodRoute : RecordGraph

    /** 운동 기록 (record/health) */
    @Serializable
    data object HealthRoute : RecordGraph
}

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

@Serializable
data object NotificationGraphBaseRoute : Route

sealed interface NotificationGraph : Route {
    /** 알림 목록 */
    @Serializable
    data object NotificationRoute : NotificationGraph
}

@Serializable
data object MyPageGraphBaseRoute : Route

sealed interface MyPageGraph : Route {
    /** 프로필 설정(이름/생년월일/로그아웃/탈퇴) */
    @Serializable
    data object ProfileEditRoute : MyPageGraph
}

@Serializable
data object MainRoute : Route
