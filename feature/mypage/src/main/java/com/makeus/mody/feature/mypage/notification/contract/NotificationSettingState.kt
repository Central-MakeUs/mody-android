package com.makeus.mody.feature.mypage.notification.contract

import com.makeus.mody.core.commonui.base.UiState

data class NotificationSettingState(
    /** 식사 및 운동 알림(끄면 식사/운동 스케줄 편집 숨김). */
    val recordReminderEnabled: Boolean = false,
    val commentEnabled: Boolean = false,
    val challengeEnabled: Boolean = false,
    // 식사 시각(null = 식사 안 함).
    val breakfastHour: Int? = 8,
    val lunchHour: Int? = 12,
    val dinnerHour: Int? = 18,
    /** 선택된 운동요일(1=월~7=일) → (hour24, minute). */
    val exerciseTimes: Map<Int, Pair<Int, Int>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
) : UiState
