package com.makeus.mody.feature.record.food.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.error.ErrorAlert

data class RecordFoodState(
    /** 선택/촬영한 사진 URI 문자열. null 이면 미선택 → 업로드 박스 노출. */
    val photoUri: String? = null,
    val menu: String = "",
    /** 식사 시간 (24h). 초기값은 진입 시각. */
    val hour24: Int = 12,
    val minute: Int = 0,
    val isPhotoSheetVisible: Boolean = false,
    /** 커스텀 촬영 오버레이 표시 여부. */
    val isCameraVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    /** 작성 실패 다이얼로그 문구. 확인 시 소비. null = 에러 없음. */
    val submitError: ErrorAlert? = null,
) : UiState {
    /** 작성 완료 활성 조건: 사진 + 메뉴 필수. */
    val canSubmit: Boolean
        get() = photoUri != null && menu.isNotBlank() && !isSubmitting
}
