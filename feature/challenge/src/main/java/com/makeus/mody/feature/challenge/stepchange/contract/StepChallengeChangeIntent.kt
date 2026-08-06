package com.makeus.mody.feature.challenge.stepchange.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class StepChallengeChangeIntent : UiIntent {
    data object ScreenEntered : StepChallengeChangeIntent()
    data object BackClicked : StepChallengeChangeIntent()

    /** 카드 탭 — 바로 바꾸지 않고 확인 다이얼로그를 띄운다. */
    data class OptionClicked(val challengeId: Long) : StepChallengeChangeIntent()
    data object ChangeConfirmed : StepChallengeChangeIntent()
    data object ChangeCancelled : StepChallengeChangeIntent()

    data object ErrorShown : StepChallengeChangeIntent()
}
