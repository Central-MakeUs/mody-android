package com.makeus.mody.feature.mypage.notification.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class NotificationSettingIntent : UiIntent {
    data object Load : NotificationSettingIntent()
    data object BackClicked : NotificationSettingIntent()

    // 토글 3개.
    data class CommentToggled(val enabled: Boolean) : NotificationSettingIntent()
    data class ChallengeToggled(val enabled: Boolean) : NotificationSettingIntent()
    data class RecordReminderToggled(val enabled: Boolean) : NotificationSettingIntent()

    // 식사/운동 스케줄.
    data class MealHoursChanged(
        val breakfast: Int?,
        val lunch: Int?,
        val dinner: Int?,
    ) : NotificationSettingIntent()

    data class ExerciseDaySet(val day: Int, val hour: Int, val minute: Int) : NotificationSettingIntent()
    data class ExerciseDayRemoved(val day: Int) : NotificationSettingIntent()
    data class ExerciseAllTimesSet(val hour: Int, val minute: Int) : NotificationSettingIntent()

    data object ErrorShown : NotificationSettingIntent()
}
