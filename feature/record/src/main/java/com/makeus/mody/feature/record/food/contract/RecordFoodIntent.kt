package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.domain.model.CropRegion

sealed class RecordFoodIntent : UiIntent {
    data object BackClicked : RecordFoodIntent()

    data object PhotoBoxClicked : RecordFoodIntent()
    data object PhotoSheetDismissed : RecordFoodIntent()

    /** 촬영하기 → 커스텀 카메라 오버레이 오픈. */
    data object TakePhotoClicked : RecordFoodIntent()
    data object CameraDismissed : RecordFoodIntent()
    data object PickFromGalleryClicked : RecordFoodIntent()

    /** 촬영/갤러리로 사진 선택. cropRegion 은 촬영(크롭) 시에만, 갤러리는 null. */
    data class PhotoSelected(val uri: String, val cropRegion: CropRegion?) : RecordFoodIntent()

    data class MenuChanged(val value: String) : RecordFoodIntent()
    data class TimeChanged(val hour24: Int, val minute: Int) : RecordFoodIntent()

    data object SubmitClicked : RecordFoodIntent()
    data object SubmitErrorShown : RecordFoodIntent()
}
