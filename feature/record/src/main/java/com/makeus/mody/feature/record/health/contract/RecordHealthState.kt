package com.makeus.mody.feature.record.health.contract

import com.makeus.mody.core.commonui.base.UiState

data class RecordHealthState(
    /** 선택/촬영한 사진 URI 문자열. null 이면 미선택 → 업로드 박스 노출. */
    val photoUri: String? = null,
    /** 선택한 운동 종류. null 이면 미선택. */
    val exerciseType: ExerciseType? = null,
    /** [ExerciseType.ETC] 선택 시 직접 입력값. */
    val customExercise: String = "",
    /** 운동 종류 드롭다운 펼침 여부. */
    val isTypeDropdownExpanded: Boolean = false,
    /** 운동 시간(시/분). */
    val durationHours: Int = 0,
    val durationMinutes: Int = 0,
    val isPhotoSheetVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    /** 작성 완료 실패 메시지 (토스트 1회 표시 후 소비). */
    val submitError: String? = null,
) : UiState {
    /** ETC 면 직접입력 텍스트필드로 전환. */
    val isCustomType: Boolean get() = exerciseType == ExerciseType.ETC

    /** 서버로 보낼 운동 종류 이름. ETC 면 직접입력값. */
    val resolvedExerciseName: String
        get() = if (isCustomType) customExercise.trim() else exerciseType?.label.orEmpty()

    /** 작성 완료 활성: 사진 + 운동 종류(이름) + 시간(0 초과) 필수. */
    val canSubmit: Boolean
        get() = photoUri != null &&
            resolvedExerciseName.isNotBlank() &&
            (durationHours > 0 || durationMinutes > 0) &&
            !isSubmitting
}
