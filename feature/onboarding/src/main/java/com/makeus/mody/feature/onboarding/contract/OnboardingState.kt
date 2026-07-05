package com.makeus.mody.feature.onboarding.contract

import com.makeus.mody.core.commonui.base.UiState

/** 시각(24h). 운동 요일별 알림 시간. */
data class TimeOfDay(val hour: Int, val minute: Int)

/**
 * 온보딩 전체 스텝에서 누적되는 단일 상태.
 * nav graph 에 scope 된 OnboardingViewModel 이 보유한다.
 */
data class OnboardingState(
    val nickname: String = "",
    val birthYear: Int = 2003,
    val birthMonth: Int = 1,
    val birthDay: Int = 19,
    val currentWeight: Int = 57,
    val targetWeight: Int = 57,
    // null = "식사 안 함" (해당 끼니 알림 끔)
    val breakfastHour: Int? = 8,
    val lunchHour: Int? = 12,
    val dinnerHour: Int? = 18,
    // 선택된 운동요일 → 알림 시각. key: 1(월)~7(일)
    val exerciseTimes: Map<Int, TimeOfDay> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState {

    /** 선택된 운동요일 집합(시각 map 의 key). */
    val exerciseDays: Set<Int>
        get() = exerciseTimes.keys

    val isNicknameValid: Boolean
        get() = nickname.isNotBlank() && nickname.length <= NICKNAME_MAX

    val isExerciseValid: Boolean
        get() = exerciseTimes.size >= EXERCISE_MIN_DAYS

    /** 목표 - 현재 체중 차이(kg). 양수=증량, 음수=감량, 0=유지 */
    val weightDiff: Int
        get() = targetWeight - currentWeight

    companion object {
        const val NICKNAME_MAX = 14
        const val EXERCISE_MIN_DAYS = 3
    }
}
