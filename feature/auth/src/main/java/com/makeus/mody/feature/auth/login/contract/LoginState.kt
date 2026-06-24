package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiState

data class LoginState(
    val isLoading: Boolean = false,
) : UiState
