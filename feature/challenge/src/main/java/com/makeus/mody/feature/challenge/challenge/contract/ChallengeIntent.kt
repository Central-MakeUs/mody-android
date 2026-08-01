package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ChallengeIntent : UiIntent {
    /** 상단 서브탭(연속 기록/챌린지) 전환. */
    data class SubTabSelected(val tab: ChallengeSubTab) : ChallengeIntent()

    /** 상단 알림 아이콘. */
    data object AlarmClicked : ChallengeIntent()
}
