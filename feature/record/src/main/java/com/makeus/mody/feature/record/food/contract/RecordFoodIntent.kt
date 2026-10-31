package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.model.CropRegion

sealed class RecordFoodIntent : UiIntent {
    data object BackClicked : RecordFoodIntent()

    data object PhotoBoxClicked : RecordFoodIntent()
    data object PhotoSheetDismissed : RecordFoodIntent()

    /** 촬영하기 → 커스텀 카메라 오버레이 오픈. */
    data object TakePhotoClicked : RecordFoodIntent()
    data object CameraDismissed : RecordFoodIntent()
    /** 갤러리에서 선택 → 오버레이를 갤러리 선택기부터 연다. */
    data object PickFromGalleryClicked : RecordFoodIntent()

    /** 촬영/갤러리 사진을 조정까지 마침. 둘 다 조정 화면을 거치므로 크롭 영역은 항상 있다. */
    data class PhotoSelected(val uri: String, val cropRegion: CropRegion) : RecordFoodIntent()

    data class MenuChanged(val value: String) : RecordFoodIntent()
    data class TimeChanged(val hour24: Int, val minute: Int) : RecordFoodIntent()

    data object SubmitClicked : RecordFoodIntent()
    data object SubmitErrorShown : RecordFoodIntent()
}
