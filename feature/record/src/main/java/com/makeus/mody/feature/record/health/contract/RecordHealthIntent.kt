package com.makeus.mody.feature.record.health.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.model.CropRegion

sealed class RecordHealthIntent : UiIntent {
    data object BackClicked : RecordHealthIntent()

    data object PhotoBoxClicked : RecordHealthIntent()
    data object PhotoSheetDismissed : RecordHealthIntent()

    /** 촬영하기 → 커스텀 카메라 오버레이 오픈. */
    data object TakePhotoClicked : RecordHealthIntent()
    data object CameraDismissed : RecordHealthIntent()
    /** 갤러리에서 선택 → 오버레이를 갤러리 선택기부터 연다. */
    data object PickFromGalleryClicked : RecordHealthIntent()

    /** 촬영/갤러리 사진을 조정까지 마침. 둘 다 조정 화면을 거치므로 크롭 영역은 항상 있다. */
    data class PhotoSelected(val uri: String, val cropRegion: CropRegion) : RecordHealthIntent()

    data object TypeDropdownToggled : RecordHealthIntent()
    data object TypeDropdownDismissed : RecordHealthIntent()
    data class TypeSelected(val type: ExerciseType) : RecordHealthIntent()
    data class CustomExerciseChanged(val value: String) : RecordHealthIntent()

    /** 기타 직접입력 X → 종류 미선택으로 되돌려 다시 선택 가능하게. */
    data object CustomExerciseCleared : RecordHealthIntent()

    data class DurationChanged(val hours: Int, val minutes: Int) : RecordHealthIntent()

    data object SubmitClicked : RecordHealthIntent()
    data object SubmitErrorShown : RecordHealthIntent()
}
