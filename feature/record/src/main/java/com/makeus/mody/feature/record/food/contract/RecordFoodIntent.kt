package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordFoodIntent : UiIntent {
    data object BackClicked : RecordFoodIntent()

    data object PhotoBoxClicked : RecordFoodIntent()
    data object PhotoSheetDismissed : RecordFoodIntent()

    /** 촬영하기 → 커스텀 카메라 오버레이 오픈. */
    data object TakePhotoClicked : RecordFoodIntent()
    data object CameraDismissed : RecordFoodIntent()
    data object PickFromGalleryClicked : RecordFoodIntent()
    data class PhotoSelected(val uri: String) : RecordFoodIntent()

    data class MenuChanged(val value: String) : RecordFoodIntent()
    data class TimeChanged(val hour24: Int, val minute: Int) : RecordFoodIntent()

    data object SubmitClicked : RecordFoodIntent()
    data object SubmitErrorShown : RecordFoodIntent()
}
