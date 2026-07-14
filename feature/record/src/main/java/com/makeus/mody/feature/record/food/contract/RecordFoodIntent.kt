package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordFoodIntent : UiIntent {
    data object BackClicked : RecordFoodIntent()

    data object PhotoBoxClicked : RecordFoodIntent()
    data object PhotoSheetDismissed : RecordFoodIntent()
    data object TakePhotoClicked : RecordFoodIntent()
    data object PickFromGalleryClicked : RecordFoodIntent()

    data class MenuChanged(val value: String) : RecordFoodIntent()
    data class TimeChanged(val hour24: Int, val minute: Int) : RecordFoodIntent()

    data object SubmitClicked : RecordFoodIntent()
}
