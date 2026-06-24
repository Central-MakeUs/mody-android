package com.makeus.mody.feature.auth.group.contract

import com.makeus.mody.core.commonui.base.UiState

enum class GroupMode { Select, Join, Create }

data class GroupState(
    val mode: GroupMode = GroupMode.Select,
    val groupCode: String = "",
    val groupName: String = "",
    val isLoading: Boolean = false,
) : UiState
