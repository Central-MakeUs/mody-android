package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.error.ErrorAlert

data class LoginState(
    val isLoading: Boolean = false,
    /** 로그인 실패 다이얼로그 문구. null = 에러 없음. */
    val error: ErrorAlert? = null,
) : UiState
