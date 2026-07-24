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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ERROR_MESSAGE = "잠시 후 다시 시도해주세요."

/** 토글 연타를 합치는 디바운스 시간. */
private const val TOGGLE_DEBOUNCE_MS = 400L

/** 스케줄(끼니·운동) 연속 편집을 합치는 디바운스 시간. 드래그를 감안해 토글보다 약간 길게. */
private const val SCHEDULE_DEBOUNCE_MS = 500L

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<NotificationSettingState, NotificationSettingIntent>(NotificationSettingState()) {

    // 토글 디바운스 job. 새 토글마다 리셋.
    private var toggleSyncJob: Job? = null

    // 스케줄 디바운스 job. 새 편집마다 리셋.
    private var scheduleSyncJob: Job? = null

    init {
        load()
    }

    override suspend fun processIntent(intent: NotificationSettingIntent) {
        when (intent) {
            is NotificationSettingIntent.Load -> load()
            is NotificationSettingIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is NotificationSettingIntent.CommentToggled -> {
                setState { copy(commentEnabled = intent.enabled) }
                scheduleToggleSync()
            }

            is NotificationSettingIntent.ChallengeToggled -> {
                setState { copy(challengeEnabled = intent.enabled) }
                scheduleToggleSync()
            }

            is NotificationSettingIntent.RecordReminderToggled -> {
                setState { copy(recordReminderEnabled = intent.enabled) }
                scheduleToggleSync()
            }

            is NotificationSettingIntent.MealHoursChanged -> {
                setState { copy(breakfastHour = intent.breakfast, lunchHour = intent.lunch, dinnerHour = intent.dinner) }
                scheduleSync()
            }

            is NotificationSettingIntent.ExerciseDaySet -> {
                setState { copy(exerciseTimes = exerciseTimes + (intent.day to (intent.hour to intent.minute))) }
                scheduleSync()
            }

            is NotificationSettingIntent.ExerciseDayRemoved -> {
                setState { copy(exerciseTimes = exerciseTimes - intent.day) }
                scheduleSync()
            }

            is NotificationSettingIntent.ExerciseAllTimesSet -> {
                setState {
                    copy(exerciseTimes = exerciseTimes.mapValues { intent.hour to intent.minute })
                }
                scheduleSync()
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

    /**
     * 토글 변경을 디바운스해 PATCH. 연타(예: 3개 연속 토글)를 마지막 상태 1회 전송으로 합친다.
     * 매번 새 변경이 오면 이전 대기 job 을 취소해 타이머를 리셋한다.
     */
    private fun scheduleToggleSync() {
        toggleSyncJob?.cancel()
        toggleSyncJob = viewModelScope.launch {
            delay(TOGGLE_DEBOUNCE_MS)
            try {
                pushToggles()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 저장 실패 → 에러 노출 + 서버값으로 재동기화(낙관적 UI 정정).
                setState { copy(error = ERROR_MESSAGE) }
                runCatching { applySettings(myPageRepository.refreshNotificationSettings()) }
            }
        }
    }

    /**
     * 토글 3개 전체를 서버에 전송.
     * 서버 PATCH 가 "보낸 필드만 적용, 나머지는 false 로 리셋"(전체 교체) 하므로,
     * 변경된 하나만 보내면 나머지 토글이 꺼진다. 항상 현재 상태 3개를 함께 보내 보존한다.
     * TODO(server): PATCH 가 부분 수정(안 보낸 필드 미변경)으로 고쳐지면 변경 필드만 전송하도록 원복.
     */
    private suspend fun pushToggles() {
        val s = currentState
        myPageRepository.updateNotificationToggles(
            recordReminderEnabled = s.recordReminderEnabled,
            commentNotificationEnabled = s.commentEnabled,
            challengeNotificationEnabled = s.challengeEnabled,
        )
    }

    /**
     * 스케줄 변경을 디바운스해 PUT. 끼니/운동을 연속으로 편집(드래그·연타)하면 매 변경마다
     * PUT 이 쏟아지고, 화면을 벗어나면 viewModelScope 취소로 in-flight 요청이 전부 죽어
     * 아무것도 저장되지 않는다. 마지막 상태 1회 전송으로 합쳐 요청 폭풍·취소 창을 줄인다.
     */
    private fun scheduleSync() {
        scheduleSyncJob?.cancel()
        scheduleSyncJob = viewModelScope.launch {
            delay(SCHEDULE_DEBOUNCE_MS)
            try {
                pushSchedules()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 실패 → 에러 노출 + 서버값으로 재동기화(낙관적 편집 정정).
                setState { copy(error = ERROR_MESSAGE) }
                runCatching { applySettings(myPageRepository.refreshNotificationSettings()) }
            }
        }
    }

    /** 현재 상태의 식사(3)/운동 스케줄 전체를 PUT. */
    private suspend fun pushSchedules() {
        val s = currentState
        val meals = listOf(
            meal(MealType.BREAKFAST, s.breakfastHour),
            meal(MealType.LUNCH, s.lunchHour),
            meal(MealType.DINNER, s.dinnerHour),
        )
        val exercises = s.exerciseTimes.map { (day, t) ->
            ExerciseSchedule(dayOfWeek = day, hour = t.first, minute = t.second)
        }
        myPageRepository.updateSchedules(meals, exercises)
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
