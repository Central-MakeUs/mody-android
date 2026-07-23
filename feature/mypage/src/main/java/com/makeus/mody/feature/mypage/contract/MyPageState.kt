package com.makeus.mody.feature.mypage.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.WeightSummary

data class MyPageState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val daysTogether: Int = 0,
    val weight: WeightSummary? = null,
    val isLoading: Boolean = false,
    /** 체중 기록 바텀시트 표시 여부. */
    val showWeightSheet: Boolean = false,
    /** 체중 기록 저장 중. */
    val isRecordingWeight: Boolean = false,
    /** 체중 기록 저장 실패 메시지(토스트 후 소비). */
    val weightError: String? = null,
) : UiState
