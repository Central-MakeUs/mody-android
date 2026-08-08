package com.makeus.mody.feature.challenge.weeklydetail.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.domain.model.CropRegion

sealed class WeeklyChallengeDetailIntent : UiIntent {
    data object ScreenEntered : WeeklyChallengeDetailIntent()
    data object BackClicked : WeeklyChallengeDetailIntent()

    /** "+ 인증하기" 탭 → 촬영 오버레이. */
    data object AddProofClicked : WeeklyChallengeDetailIntent()

    /** 오버레이 닫기/취소. */
    data object CameraDismissed : WeeklyChallengeDetailIntent()

    /** 촬영·조정 완료. 원본 uri + 정규화 크롭 영역. */
    data class PhotoCaptured(
        val imageUri: String,
        val cropRegion: CropRegion,
    ) : WeeklyChallengeDetailIntent()

    data object ShareClicked : WeeklyChallengeDetailIntent()

    /** 공유 시트를 띄운 뒤 — URI 를 한 번만 소비하도록. */
    data object ShareLaunched : WeeklyChallengeDetailIntent()

    data object ToastShown : WeeklyChallengeDetailIntent()
    data object ErrorShown : WeeklyChallengeDetailIntent()
}
