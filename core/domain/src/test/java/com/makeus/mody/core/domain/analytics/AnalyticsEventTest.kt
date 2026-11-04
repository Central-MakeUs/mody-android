package com.makeus.mody.core.domain.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 이벤트 이름·파라미터가 "MODY GA4 이벤트 명세서 v2" 와 같은지 본다.
 * 여기 값이 바뀌면 GA4 보고서·맞춤 측정기준이 끊긴다 — 명세가 먼저 바뀐 경우에만 고친다.
 */
class AnalyticsEventTest {

    @Test
    fun `이벤트 이름과 파라미터가 명세와 같다`() {
        val expected = listOf(
            AnalyticsEvent.Login(LoginMethod.KAKAO) to ("login" to mapOf("method" to "kakao")),
            AnalyticsEvent.LoginFail(LoginMethod.GOOGLE, LoginFailReason.CANCEL) to
                ("login_fail" to mapOf("method" to "google", "fail_reason" to "cancel")),
            AnalyticsEvent.OnboardingStepComplete(OnboardingStep.TERMS) to
                ("onboarding_step_complete" to mapOf("step_name" to "terms")),
            AnalyticsEvent.SameTimeClick(3, ScheduleEntrySource.ONBOARDING) to
                ("same_time_click" to mapOf("selected_day_count" to "3", "entry_source" to "onboarding")),
            AnalyticsEvent.ExerciseTimeSheetClose(2, ScheduleEntrySource.MYPAGE) to
                ("exercise_time_sheet_close" to mapOf("selected_day_count" to "2", "entry_source" to "mypage")),
            AnalyticsEvent.ExerciseTimeSheetApply(7, ScheduleEntrySource.MYPAGE) to
                ("exercise_time_sheet_apply" to mapOf("selected_day_count" to "7", "entry_source" to "mypage")),
            AnalyticsEvent.PermissionResult(PermissionType.HEALTH_CONNECT, granted = false) to
                ("permission_result" to mapOf("permission_type" to "health_connect", "grant_status" to "denied")),
            AnalyticsEvent.SignUp(LoginMethod.REVIEW) to ("sign_up" to mapOf("method" to "review")),
            AnalyticsEvent.GroupSelectorClick(2) to ("group_selector_click" to mapOf("group_count" to "2")),
            AnalyticsEvent.GroupAddClick(1) to ("group_add_click" to mapOf("group_count" to "1")),
            AnalyticsEvent.PokeCtaClick to ("poke_cta_click" to emptyMap()),
            AnalyticsEvent.JoinGroup(GroupEntryPoint.SIGNUP) to ("join_group" to mapOf("entry_source" to "signup")),
            AnalyticsEvent.GroupJoinFail(GroupJoinFailReason.INVALID_CODE, GroupEntryPoint.REJOIN) to
                ("group_join_fail" to mapOf("fail_reason" to "invalid_code", "entry_source" to "rejoin")),
            AnalyticsEvent.GroupCreateClick(GroupEntryPoint.FEED) to
                ("group_create_click" to mapOf("entry_source" to "feed")),
            AnalyticsEvent.GroupCreateSuccess(GroupEntryPoint.FEED) to
                ("group_create_success" to mapOf("entry_source" to "feed")),
            AnalyticsEvent.GroupShareClick(ShareMethod.COPY) to ("group_share_click" to mapOf("share_method" to "copy")),
            AnalyticsEvent.GroupCreateFinish to ("group_create_finish" to emptyMap()),
            AnalyticsEvent.PokeSend to ("poke_send" to emptyMap()),
            AnalyticsEvent.ChallengeChange("서울-인천", "서울-천안", ProgressBucket.UNDER_50) to
                (
                    "challenge_change" to mapOf(
                        "from_challenge" to "서울-인천",
                        "to_challenge" to "서울-천안",
                        "progress_bucket" to "25_50",
                    )
                    ),
            AnalyticsEvent.WeeklyChallengeComplete to ("weekly_challenge_complete" to emptyMap()),
            AnalyticsEvent.WeeklyChallengeShare to ("weekly_challenge_share" to emptyMap()),
        )

        expected.forEach { (event, spec) ->
            assertEquals(spec.first, event.name)
            assertEquals("${event.name} 파라미터", spec.second, event.params)
        }
    }

    @Test
    fun `GA4 이름 규칙을 지킨다`() {
        val names = listOf(
            AnalyticsEvent.Login(LoginMethod.KAKAO),
            AnalyticsEvent.ExerciseTimeSheetApply(1, ScheduleEntrySource.ONBOARDING),
            AnalyticsEvent.ChallengeChange("a", "b", ProgressBucket.UNDER_25),
            AnalyticsEvent.WeeklyChallengeComplete,
        ).flatMap { listOf(it.name) + it.params.keys } + AnalyticsUserProperty.GROUP_COUNT

        names.forEach { name ->
            assertTrue(name, name.matches(Regex("[a-z][a-z0-9_]{0,39}")))
            assertTrue(name, listOf("firebase_", "google_", "ga_").none(name::startsWith))
        }
    }

    @Test
    fun `진행률은 25 단위 구간으로 묶는다`() {
        assertEquals(ProgressBucket.UNDER_25, ProgressBucket.of(0))
        assertEquals(ProgressBucket.UNDER_25, ProgressBucket.of(24))
        assertEquals(ProgressBucket.UNDER_50, ProgressBucket.of(25))
        assertEquals(ProgressBucket.UNDER_75, ProgressBucket.of(74))
        assertEquals(ProgressBucket.UP_TO_100, ProgressBucket.of(75))
        assertEquals(ProgressBucket.UP_TO_100, ProgressBucket.of(100))
    }
}
