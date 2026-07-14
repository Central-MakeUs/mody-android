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
) : UiState
