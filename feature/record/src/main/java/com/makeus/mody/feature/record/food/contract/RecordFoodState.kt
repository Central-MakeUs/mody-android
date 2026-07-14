package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiState

data class RecordFoodState(
    /** 선택/촬영한 사진 URI 문자열. null 이면 미선택 → 업로드 박스 노출. */
    val photoUri: String? = null,
    val menu: String = "",
    /** 식사 시간 (24h). 초기값은 진입 시각. */
    val hour24: Int = 12,
    val minute: Int = 0,
    val isPhotoSheetVisible: Boolean = false,
    val isSubmitting: Boolean = false,
) : UiState {
    /** 작성 완료 활성 조건: 사진 + 메뉴 필수. */
    val canSubmit: Boolean
        get() = photoUri != null && menu.isNotBlank() && !isSubmitting
}
