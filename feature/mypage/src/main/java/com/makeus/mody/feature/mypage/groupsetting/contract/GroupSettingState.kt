package com.makeus.mody.feature.mypage.groupsetting.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.Group

data class GroupSettingState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    /** null 이 아니면 해당 그룹의 나가기 확인 다이얼로그 표시. */
    val leaveTarget: Group? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
) : UiState
