package com.makeus.mody.feature.mypage.notification

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MealType
import com.makeus.mody.core.domain.model.NotificationSettings
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.notification.contract.NotificationSettingIntent
import com.makeus.mody.feature.mypage.notification.contract.NotificationSettingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ERROR_MESSAGE = "잠시 후 다시 시도해주세요."

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<NotificationSettingState, NotificationSettingIntent>(NotificationSettingState()) {

    init {
        load()
    }

    override suspend fun processIntent(intent: NotificationSettingIntent) {
        when (intent) {
            is NotificationSettingIntent.Load -> load()
            is NotificationSettingIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is NotificationSettingIntent.CommentToggled -> {
                val prev = currentState.commentEnabled
                setState { copy(commentEnabled = intent.enabled) }
                patchToggle(revert = { setState { copy(commentEnabled = prev) } }) {
                    myPageRepository.updateNotificationToggles(commentNotificationEnabled = intent.enabled)
                }
            }

            is NotificationSettingIntent.ChallengeToggled -> {
                val prev = currentState.challengeEnabled
                setState { copy(challengeEnabled = intent.enabled) }
                patchToggle(revert = { setState { copy(challengeEnabled = prev) } }) {
                    myPageRepository.updateNotificationToggles(challengeNotificationEnabled = intent.enabled)
                }
            }

            is NotificationSettingIntent.RecordReminderToggled -> {
                val prev = currentState.recordReminderEnabled
                setState { copy(recordReminderEnabled = intent.enabled) }
                patchToggle(revert = { setState { copy(recordReminderEnabled = prev) } }) {
                    myPageRepository.updateNotificationToggles(recordReminderEnabled = intent.enabled)
                }
            }

            is NotificationSettingIntent.MealHoursChanged -> {
                setState { copy(breakfastHour = intent.breakfast, lunchHour = intent.lunch, dinnerHour = intent.dinner) }
                persistSchedules()
            }

            is NotificationSettingIntent.ExerciseDaySet -> {
                setState { copy(exerciseTimes = exerciseTimes + (intent.day to (intent.hour to intent.minute))) }
                persistSchedules()
            }

            is NotificationSettingIntent.ExerciseDayRemoved -> {
                setState { copy(exerciseTimes = exerciseTimes - intent.day) }
                persistSchedules()
            }

            is NotificationSettingIntent.ExerciseAllTimesSet -> {
                setState {
                    copy(exerciseTimes = exerciseTimes.mapValues { intent.hour to intent.minute })
                }
                persistSchedules()
            }

            is NotificationSettingIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    /**
     * 캐시 우선 + 백그라운드 재동기화(stale-while-revalidate).
     * 캐시가 있으면 즉시 표시해 진입 스피너를 없애고, 서버값으로 조용히 갱신한다.
     * 콜드(캐시 없음)에서만 스피너 1회.
     */
    private fun load() = viewModelScope.launch {
        val cached = runCatching { myPageRepository.getCachedNotificationSettings() }.getOrNull()
        if (cached != null) {
            applySettings(cached)
        } else {
            setState { copy(isLoading = true) }
        }
        try {
            applySettings(myPageRepository.refreshNotificationSettings())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 캐시가 있으면 조용히 무시(오프라인 등 stale 유지), 없으면 에러 노출.
            setState { copy(isLoading = false, error = if (cached == null) ERROR_MESSAGE else error) }
        }
    }

    private fun applySettings(s: NotificationSettings) = setState {
        copy(
            recordReminderEnabled = s.recordReminderEnabled,
            commentEnabled = s.commentNotificationEnabled,
            challengeEnabled = s.challengeNotificationEnabled,
            // 끼니가 응답에 있으면 그 값(skipped=null), 없으면 기존 기본값 유지.
            breakfastHour = s.meals.hourOf(MealType.BREAKFAST, breakfastHour),
            lunchHour = s.meals.hourOf(MealType.LUNCH, lunchHour),
            dinnerHour = s.meals.hourOf(MealType.DINNER, dinnerHour),
            exerciseTimes = s.exercises.associate { it.dayOfWeek to (it.hour to it.minute) },
            isLoading = false,
        )
    }

    /** 토글 PATCH. 실패 시 revert + 에러. */
    private fun patchToggle(revert: () -> Unit, block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            revert()
            setState { copy(error = ERROR_MESSAGE) }
        }
    }

    /** 현재 상태의 식사/운동 스케줄 전체를 PUT. 실패 시 서버값으로 재동기화 + 에러. */
    private fun persistSchedules() = viewModelScope.launch {
        val s = currentState
        val meals = listOf(
            meal(MealType.BREAKFAST, s.breakfastHour),
            meal(MealType.LUNCH, s.lunchHour),
            meal(MealType.DINNER, s.dinnerHour),
        )
        val exercises = s.exerciseTimes.map { (day, t) ->
            ExerciseSchedule(dayOfWeek = day, hour = t.first, minute = t.second)
        }
        try {
            myPageRepository.updateSchedules(meals, exercises)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 실패해도 로컬 편집은 유지(load()로 깜빡임/편집 소실 방지). 다음 진입 시 서버값으로 재동기화.
            setState { copy(error = ERROR_MESSAGE) }
        }
    }
}

/** hour==null → 식사 안 함(skipped). 시각은 서버 필수라 기본값으로 채움. */
private fun meal(type: MealType, hour: Int?): MealSchedule =
    MealSchedule(type = type, hour = hour ?: 0, minute = 0, skipped = hour == null)

/** 해당 끼니가 응답에 있으면 시각(skipped면 null), 없으면 [fallback]. */
private fun List<MealSchedule>.hourOf(type: MealType, fallback: Int?): Int? {
    val m = firstOrNull { it.type == type } ?: return fallback
    return if (m.skipped) null else m.hour
}
