package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ChallengeIntent : UiIntent {
    /** 탭 진입/복귀 시 재조회. */
    data object ScreenEntered : ChallengeIntent()

    /** 상단 서브탭(연속 기록/챌린지) 전환. */
    data class SubTabSelected(val tab: ChallengeSubTab) : ChallengeIntent()

    /** 상단 알림 아이콘. */
    data object AlarmClicked : ChallengeIntent()

    /** 버디 콕 찌르기. */
    data class NudgeClicked(val memberId: Long) : ChallengeIntent()

    /** 걸음 수 새로고침(달성 걸음수 옆 리셋 아이콘). */
    data object StepRefreshClicked : ChallengeIntent()

    /** 걸음 수 챌린지 변경. */
    data object ChangeStepChallengeClicked : ChallengeIntent()

    /** 주간 챌린지 항목 → 상세. */
    data class WeeklyChallengeClicked(val groupChallengeId: Long) : ChallengeIntent()

    /** 에러 다이얼로그 확인. */
    data object ErrorShown : ChallengeIntent()
}
