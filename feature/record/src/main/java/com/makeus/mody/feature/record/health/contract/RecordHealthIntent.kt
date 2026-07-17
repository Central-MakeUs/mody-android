package com.makeus.mody.feature.record.health.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordHealthIntent : UiIntent {
    data object BackClicked : RecordHealthIntent()

    data object PhotoBoxClicked : RecordHealthIntent()
    data object PhotoSheetDismissed : RecordHealthIntent()
    data object TakePhotoClicked : RecordHealthIntent()
    data object PickFromGalleryClicked : RecordHealthIntent()
    data class PhotoSelected(val uri: String) : RecordHealthIntent()

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
