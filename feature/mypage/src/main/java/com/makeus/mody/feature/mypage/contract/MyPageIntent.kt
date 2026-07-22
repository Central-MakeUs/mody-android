package com.makeus.mody.feature.mypage.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class MyPageIntent : UiIntent {
    /** 화면 진입/재개 시 데이터 로드. */
    data object Refresh : MyPageIntent()

    data object AlarmClicked : MyPageIntent()
    data object ProfileSettingClicked : MyPageIntent()

    /** "체중 기록하기" → 바텀시트 오픈. */
    data object WeightRecordClicked : MyPageIntent()

    /** 바텀시트 닫기(취소/스크림). */
    data object WeightRecordDismissed : MyPageIntent()

    /** "기록 완료" → 서버 저장. recordedOn: ISO(yyyy-MM-dd). */
    data class WeightRecordSubmitted(val recordedOn: String, val weightKg: Double) : MyPageIntent()
    data object NotificationSettingClicked : MyPageIntent()
    data object GroupSettingClicked : MyPageIntent()
    data object HealthDataSettingClicked : MyPageIntent()
}
