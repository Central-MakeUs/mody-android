package com.makeus.mody.feature.mypage.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class MyPageIntent : UiIntent {
    /** 화면 진입/재개 시 데이터 로드. */
    data object Refresh : MyPageIntent()

    data object AlarmClicked : MyPageIntent()
    data object ProfileSettingClicked : MyPageIntent()
    data object WeightRecordClicked : MyPageIntent()
    data object NotificationSettingClicked : MyPageIntent()
    data object GroupSettingClicked : MyPageIntent()
    data object HealthDataSettingClicked : MyPageIntent()
}
