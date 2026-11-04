package com.makeus.mody.feature.onboarding

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.analytics.AnalyticsEvent
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import com.makeus.mody.core.domain.analytics.LoginMethod
import com.makeus.mody.core.domain.analytics.OnboardingStep
import com.makeus.mody.core.domain.analytics.ScheduleEntrySource
import com.makeus.mody.core.domain.analytics.log
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.model.ExerciseSchedule
import com.makeus.mody.core.model.MealSchedule
import com.makeus.mody.core.model.MealType
import com.makeus.mody.core.model.OnboardingProfile
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.feature.onboarding.contract.OnboardingIntent
import com.makeus.mody.feature.onboarding.contract.OnboardingState
import com.makeus.mody.feature.onboarding.contract.TimeOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
    private val onboardingRepository: OnboardingRepository,
    private val sessionRepository: SessionRepository,
    private val analyticsLogger: AnalyticsLogger,
) : BaseViewModel<OnboardingState, OnboardingIntent>(OnboardingState()) {

    override suspend fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NicknameChanged ->
                setState { copy(nickname = intent.value) }

            is OnboardingIntent.BirthChanged ->
                setState { copy(birthYear = intent.year, birthMonth = intent.month, birthDay = intent.day) }

            is OnboardingIntent.WeightChanged ->
                setState { copy(currentWeight = intent.current, targetWeight = intent.target) }

            is OnboardingIntent.MealHourChanged ->
                setState {
                    copy(
                        breakfastHour = intent.breakfast,
                        lunchHour = intent.lunch,
                        dinnerHour = intent.dinner,
                    )
                }

            is OnboardingIntent.ExerciseDaySet ->
                if (intent.day in 1..7) {
                    setState {
                        copy(exerciseTimes = exerciseTimes + (intent.day to TimeOfDay(intent.hour, intent.minute)))
                    }
                }

            is OnboardingIntent.ExerciseDayRemoved ->
                setState { copy(exerciseTimes = exerciseTimes - intent.day) }

            is OnboardingIntent.ExerciseAllTimesSet -> {
                analyticsLogger.log(
                    AnalyticsEvent.ExerciseTimeSheetApply(
                        currentState.exerciseTimes.size,
                        ScheduleEntrySource.ONBOARDING,
                    ),
                )
                setState {
                    val t = TimeOfDay(intent.hour, intent.minute)
                    copy(exerciseTimes = exerciseTimes.mapValues { t })
                }
            }

            is OnboardingIntent.SameTimeClicked -> analyticsLogger.log(
                AnalyticsEvent.SameTimeClick(currentState.exerciseTimes.size, ScheduleEntrySource.ONBOARDING),
            )

            is OnboardingIntent.SameTimeSheetDismissed -> analyticsLogger.log(
                AnalyticsEvent.ExerciseTimeSheetClose(
                    currentState.exerciseTimes.size,
                    ScheduleEntrySource.ONBOARDING,
                ),
            )

            // 스텝 순서: 닉네임 → 생년월일 → 체중 → 알림 → 완료
            // 진행 불변조건을 reducer 에서 강제 (UI 게이팅 우회 방지)
            is OnboardingIntent.NicknameNext ->
                if (currentState.isNicknameValid) navigate(OnboardingGraph.BirthRoute)
            is OnboardingIntent.BirthNext -> navigate(OnboardingGraph.WeightRoute)
            is OnboardingIntent.WeightNext -> navigate(OnboardingGraph.AlarmRoute)
            is OnboardingIntent.AlarmNext ->
                if (currentState.isExerciseValid) complete()
        }
    }

    private fun navigate(route: OnboardingGraph) {
        navigationHelper.navigate(NavigationEvent.To(route))
    }

    private suspend fun complete() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true, errorMessage = null) }
        try {
            onboardingRepository.submitProfile(currentState.toProfile())
            logSignUp()
            // 권한 요청 화면으로. 온보딩/로그인 백스택 제거(뒤로가기로 복귀 방지).
            // 권한 화면 "확인" 후 그룹 그래프로 핸드오프한다.
            navigationHelper.navigate(NavigationEvent.To(OnboardingGraph.PermissionRoute, popUpTo = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isLoading = false, errorMessage = "저장에 실패했어요. 다시 시도해주세요.") }
        }
    }

    /**
     * 알림 단계 완료(ON-13)와 가입(ON-16). 프로필 저장 성공이 곧 알림 단계 '다음' 성공이라
     * 두 이벤트가 같은 순간에 난다.
     *
     * 가입 방식은 로그인 때 저장해 둔 마지막 소셜 타입. 심사용 로그인은 그 값을 남기지 않고,
     * 로그아웃이 지우므로 비어 있으면 심사용이다.
     */
    private suspend fun logSignUp() {
        analyticsLogger.log(AnalyticsEvent.OnboardingStepComplete(OnboardingStep.NOTIFICATION))
        val loginType = try {
            sessionRepository.getLastLoginType()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        val method = loginType?.let(LoginMethod::of) ?: LoginMethod.REVIEW
        analyticsLogger.log(AnalyticsEvent.SignUp(method))
    }
}

private fun OnboardingState.toProfile(): OnboardingProfile = OnboardingProfile(
    nickname = nickname,
    birthYear = birthYear,
    birthMonth = birthMonth,
    birthDay = birthDay,
    currentWeightKg = currentWeight,
    targetWeightKg = targetWeight,
    meals = listOf(
        meal(MealType.BREAKFAST, breakfastHour),
        meal(MealType.LUNCH, lunchHour),
        meal(MealType.DINNER, dinnerHour),
    ),
    exercises = exerciseTimes.toSortedMap().map { (day, t) ->
        ExerciseSchedule(dayOfWeek = day, hour = t.hour, minute = t.minute)
    },
)

// hour==null → "식사 안 함"(skipped). 시각은 서버 필수라 기본값으로 채움.
private fun meal(type: MealType, hour: Int?): MealSchedule = MealSchedule(
    type = type,
    hour = hour ?: 0,
    minute = 0,
    skipped = hour == null,
)
