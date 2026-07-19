package com.makeus.mody.feature.mypage.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.WeightSummary

data class MyPageState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val daysTogether: Int = 0,
    val weight: WeightSummary? = null,
    val isLoading: Boolean = false,
) : UiState
