package com.makeus.mody.feature.mypage.groupsetting.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.core.domain.model.error.ErrorAlert

data class GroupSettingState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    /** null 이 아니면 해당 그룹의 나가기 확인 다이얼로그 표시. */
    val leaveTarget: Group? = null,
    val isProcessing: Boolean = false,
    /** 에러 다이얼로그 문구. 확인 시 소비. null = 에러 없음. */
    val error: ErrorAlert? = null,
) : UiState
