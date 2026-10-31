package com.makeus.mody.core.domain.analytics

import com.makeus.mody.core.model.SocialLoginType

/**
 * GA4 맞춤·권장 이벤트. "MODY GA4 이벤트 명세서 v2" 의 2_이벤트명세와 1:1 이다.
 * KDoc 의 `ON-02` 같은 번호가 명세의 No 열 — QA(DebugView) 때 이 번호로 대조한다.
 *
 * 이름·파라미터 키·값은 GA4 보고서와 맞춤 측정기준 등록에 그대로 묶여 있다. 바꾸면 그날부터
 * 다른 측정기준으로 쌓이므로 명세를 먼저 고친다. screen_view 는 여기 없다 — [AnalyticsLogger.logScreenView].
 *
 * 숫자도 문자열로 보낸다(명세 결정: 분포만 보고 평균·합계는 보지 않는다).
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
) {
    /** ON-02. 서버 로그인 성공 직후. GA4 권장 이벤트. */
    class Login(method: LoginMethod) : AnalyticsEvent("login", mapOf(METHOD to method.value))

    /** ON-03. 소셜 로그인 취소 또는 실패 응답. */
    class LoginFail(method: LoginMethod, reason: LoginFailReason) : AnalyticsEvent(
        "login_fail",
        mapOf(METHOD to method.value, FAIL_REASON to reason.value),
    )

    /** ON-05(약관) · ON-13(알림). 단계 '다음' 처리 성공. */
    class OnboardingStepComplete(step: OnboardingStep) :
        AnalyticsEvent("onboarding_step_complete", mapOf("step_name" to step.value))

    /** ON-10. '모두 같은 시간으로 설정' 클릭 = 일괄 설정 시트 열림. */
    class SameTimeClick(selectedDayCount: Int, entry: ScheduleEntrySource) :
        AnalyticsEvent("same_time_click", scheduleParams(selectedDayCount, entry))

    /**
     * ON-11. 일괄 설정 시트를 적용 없이 닫음.
     * 요일별 시간 행으로 연 시트도 같은 부품이지만 명세(O2)가 일괄 설정만 보므로 보내지 않는다.
     */
    class ExerciseTimeSheetClose(selectedDayCount: Int, entry: ScheduleEntrySource) :
        AnalyticsEvent("exercise_time_sheet_close", scheduleParams(selectedDayCount, entry))

    /** ON-12. 일괄 설정 시트에서 '확인'. 요일별 시트는 제외(ON-11 과 같은 이유). */
    class ExerciseTimeSheetApply(selectedDayCount: Int, entry: ScheduleEntrySource) :
        AnalyticsEvent("exercise_time_sheet_apply", scheduleParams(selectedDayCount, entry))

    /** ON-15. 시스템 권한 팝업에 응답한 직후. 팝업이 실제로 뜬 경우만 보낸다. */
    class PermissionResult(type: PermissionType, granted: Boolean) : AnalyticsEvent(
        "permission_result",
        mapOf(
            "permission_type" to type.value,
            "grant_status" to if (granted) "granted" else "denied",
        ),
    )

    /** ON-16. 프로필 저장 성공 직후. GA4 권장 이벤트. */
    class SignUp(method: LoginMethod) : AnalyticsEvent("sign_up", mapOf(METHOD to method.value))

    /** HM-02. 피드 상단 그룹명 클릭 → 그룹 목록 시트 열림. */
    class GroupSelectorClick(groupCount: Int) :
        AnalyticsEvent("group_selector_click", mapOf(GROUP_COUNT to groupCount.toString()))

    /** HM-03. 그룹 목록 시트 '그룹 추가하기'. 최대 그룹 수면 버튼이 없어 발생하지 않는다. */
    class GroupAddClick(groupCount: Int) :
        AnalyticsEvent("group_add_click", mapOf(GROUP_COUNT to groupCount.toString()))

    /** HM-04. 빈 피드의 '콕 찌르기 하러 가기'. */
    data object PokeCtaClick : AnalyticsEvent("poke_cta_click")

    /** GR-02. 그룹 코드 참여 서버 성공. GA4 권장 이벤트. group_id 는 보내지 않는다. */
    class JoinGroup(entry: GroupEntryPoint) :
        AnalyticsEvent("join_group", mapOf(ENTRY_SOURCE to entry.value))

    /** GR-03. 그룹 코드 참여 실패 응답. */
    class GroupJoinFail(reason: GroupJoinFailReason, entry: GroupEntryPoint) : AnalyticsEvent(
        "group_join_fail",
        mapOf(FAIL_REASON to reason.value, ENTRY_SOURCE to entry.value),
    )

    /**
     * GR-04. 그룹 생성 시작. 그룹 참여 화면의 '새로운 그룹 만들기'와 피드 그룹 추가 모달의
     * '그룹 생성하기' 둘 다 — 피드 쪽은 참여 화면을 거치지 않아, 빼면 완주율(G3) 분모에서 빠진다.
     */
    class GroupCreateClick(entry: GroupEntryPoint) :
        AnalyticsEvent("group_create_click", mapOf(ENTRY_SOURCE to entry.value))

    /** GR-05. 서버 그룹 생성 성공. 생성 수의 기준. */
    class GroupCreateSuccess(entry: GroupEntryPoint) :
        AnalyticsEvent("group_create_success", mapOf(ENTRY_SOURCE to entry.value))

    /** GR-07. 코드 공유 화면의 카카오톡 공유 / 코드 복사. */
    class GroupShareClick(method: ShareMethod) :
        AnalyticsEvent("group_share_click", mapOf("share_method" to method.value))

    /** GR-08. 코드 공유 화면 '완료' = 생성 플로우 완주. */
    data object GroupCreateFinish : AnalyticsEvent("group_create_finish")

    /** CH-02. 콕찌르기 서버 전송 성공. 대상(target_user_id)은 보내지 않는다. */
    data object PokeSend : AnalyticsEvent("poke_send")

    /**
     * CH-03. 걸음 수 챌린지 변경 서버 성공.
     * @param from 변경 전 챌린지(서버 원본값, `출발-도착`). 화면 문구가 아니다.
     */
    class ChallengeChange(from: String, to: String, progress: ProgressBucket) : AnalyticsEvent(
        "challenge_change",
        mapOf(
            "from_challenge" to from,
            "to_challenge" to to,
            "progress_bucket" to progress.value,
        ),
    )

    /** CH-05. 주간 챌린지 사진 인증 등록 성공. 챌린지 식별 값은 보내지 않는다(Q6). */
    data object WeeklyChallengeComplete : AnalyticsEvent("weekly_challenge_complete")

    /** CH-06. 주간 챌린지 'SNS에 공유하기' 클릭. */
    data object WeeklyChallengeShare : AnalyticsEvent("weekly_challenge_share")

    private companion object {
        const val METHOD = "method"
        const val FAIL_REASON = "fail_reason"
        const val ENTRY_SOURCE = "entry_source"
        const val GROUP_COUNT = "group_count"

        fun scheduleParams(selectedDayCount: Int, entry: ScheduleEntrySource) = mapOf(
            "selected_day_count" to selectedDayCount.toString(),
            ENTRY_SOURCE to entry.value,
        )
    }
}

/** screen_view 에 붙는 파라미터. 화면 이름은 라우트에서 오고, 여기엔 성격 값만 둔다. */
object AnalyticsScreenParams {
    /** GR-01. 그룹 참여 화면 진입 경로. */
    fun groupEntry(entry: GroupEntryPoint): Map<String, String> = mapOf("entry_source" to entry.value)
}

/** 사용자 속성. 맞춤 측정기준을 '사용자' 범위로 등록해야 보고서에 보인다. */
object AnalyticsUserProperty {
    /**
     * 속한 그룹 수. 명세(HM-01)는 screen_view(Feed) 파라미터였지만, 피드 화면 노출 시점엔
     * 그룹 목록이 아직 오지 않아 값을 알 수 없다. 사용자 속성이면 이후 모든 이벤트에 붙는다.
     */
    const val GROUP_COUNT = "group_count"
}

/** `method`. 심사용 히든 로그인은 실사용 지표에서 거르도록 [REVIEW] 로 분리한다. */
enum class LoginMethod(val value: String) {
    KAKAO("kakao"),
    GOOGLE("google"),
    REVIEW("review"),
    ;

    companion object {
        fun of(type: SocialLoginType): LoginMethod = when (type) {
            SocialLoginType.KAKAO -> KAKAO
            SocialLoginType.GOOGLE -> GOOGLE
        }
    }
}

/** `fail_reason`(로그인). */
enum class LoginFailReason(val value: String) {
    CANCEL("cancel"),
    ERROR("error"),
}

/** `step_name`. */
enum class OnboardingStep(val value: String) {
    TERMS("terms"),
    NOTIFICATION("notification"),
}

/** `entry_source`(시간 설정). 온보딩·마이페이지가 같은 부품을 쓴다. */
enum class ScheduleEntrySource(val value: String) {
    ONBOARDING("onboarding"),
    MYPAGE("mypage"),
}

/** `permission_type`. 카메라는 명세상 측정하지 않는다. */
enum class PermissionType(val value: String) {
    NOTIFICATION("notification"),
    HEALTH_CONNECT("health_connect"),
}

/** `entry_source`(그룹 참여·생성). 가입 직후 / 피드에서 추가 / 전체 탈퇴 후 재참여. */
enum class GroupEntryPoint(val value: String) {
    SIGNUP("signup"),
    FEED("feed"),
    REJOIN("rejoin"),
}

/** `fail_reason`(그룹 참여). 서버 에러 코드 기준 — 서버에 '만료' 코드는 없다. */
enum class GroupJoinFailReason(val value: String) {
    INVALID_CODE("invalid_code"),
    MAX_GROUP("max_group"),
    ALREADY_JOINED("already_joined"),
    GROUP_FULL("group_full"),
    SERVER_ERROR("server_error"),
    NETWORK("network"),
}

/** `share_method`. */
enum class ShareMethod(val value: String) {
    KAKAO("kakao"),
    COPY("copy"),
}

/** `progress_bucket`. 원시 걸음 수는 값이 끝없이 늘어 (other) 로 묶이므로 구간으로 보낸다. */
enum class ProgressBucket(val value: String) {
    UNDER_25("0_25"),
    UNDER_50("25_50"),
    UNDER_75("50_75"),
    UP_TO_100("75_100"),
    ;

    companion object {
        /** @param percent 0~100. 범위 밖은 양 끝으로 붙인다. */
        fun of(percent: Int): ProgressBucket = when {
            percent < 25 -> UNDER_25
            percent < 50 -> UNDER_50
            percent < 75 -> UNDER_75
            else -> UP_TO_100
        }
    }
}
