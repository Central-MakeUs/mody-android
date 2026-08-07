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

/** 약관 상세로 열 문서 종류. TermsDetailRoute 인자 및 URL 매핑에 사용. */
enum class TermsType {
    /** 개인정보처리방침 */
    Privacy,

    /** 이용약관 */
    Service,
}

sealed interface OnboardingGraph : Route {
    // 입력 스텝 순서: 약관 동의 → 닉네임 → 생년월일 → 체중 → 알림 → 권한 요청 → (그룹)

    /** 로그인 직후 첫 스텝. 필수 약관 2개 동의 후 닉네임으로. */
    @Serializable
    data object TermsRoute : OnboardingGraph

    /** 약관 전문(웹) 상세. WebView 로 표시. */
    @Serializable
    data class TermsDetailRoute(val type: TermsType) : OnboardingGraph

    @Serializable
    data object NicknameRoute : OnboardingGraph

    @Serializable
    data object BirthRoute : OnboardingGraph

    @Serializable
    data object WeightRoute : OnboardingGraph

    @Serializable
    data object AlarmRoute : OnboardingGraph

    /** 접근 권한 요청(알림/카메라/사진/건강) 안내 + 요청. 프로필 저장 후 그룹 진입 직전. */
    @Serializable
    data object PermissionRoute : OnboardingGraph
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

    /** 알림 설정(코멘트/챌린지/식사·운동 토글 + 식사/운동 스케줄) */
    @Serializable
    data object NotificationSettingRoute : MyPageGraph

    /** 그룹 설정(그룹 나가기) */
    @Serializable
    data object GroupSettingRoute : MyPageGraph

    /** 이용약관·개인정보처리방침·문의 지원 페이지(인앱 WebView) */
    @Serializable
    data object SupportRoute : MyPageGraph
}

@Serializable
data object MainRoute : Route
