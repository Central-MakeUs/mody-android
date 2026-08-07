package com.makeus.mody.feature.challenge.weeklydetail.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class WeeklyChallengeDetailIntent : UiIntent {
    data object ScreenEntered : WeeklyChallengeDetailIntent()
    data object BackClicked : WeeklyChallengeDetailIntent()

    /** "+ 인증하기" 탭 → 카메라. */
    data object AddProofClicked : WeeklyChallengeDetailIntent()

    /** 카메라를 띄운 뒤 — 요청 플래그를 한 번만 소비하도록. */
    data object CaptureLaunched : WeeklyChallengeDetailIntent()

    /** 촬영 결과. 취소했으면 호출되지 않는다. */
    data class PhotoPicked(val imageUri: String) : WeeklyChallengeDetailIntent()

    data object ShareClicked : WeeklyChallengeDetailIntent()

    /** 공유 시트를 띄운 뒤 — URI 를 한 번만 소비하도록. */
    data object ShareLaunched : WeeklyChallengeDetailIntent()

    data object ToastShown : WeeklyChallengeDetailIntent()
    data object ErrorShown : WeeklyChallengeDetailIntent()
}
