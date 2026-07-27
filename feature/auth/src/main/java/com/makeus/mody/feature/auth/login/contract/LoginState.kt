package com.makeus.mody.feature.auth.login.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.error.ErrorAlert

data class LoginState(
    val isLoading: Boolean = false,
    /** 로그인 실패 다이얼로그 문구. null = 에러 없음. */
    val error: ErrorAlert? = null,
    /** 심사용 히든 로그인 다이얼로그 표시 여부(로고 연타로 진입). */
    val showReviewLogin: Boolean = false,
    val reviewPassword: String = "",
    /** 비밀번호 불일치 표시. */
    val reviewPasswordError: Boolean = false,
) : UiState
