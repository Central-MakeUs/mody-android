package com.makeus.mody.core.domain.model

/**
 * 로그인 응답이 내려주는 진행 상태 flag.
 * 앱 시작/로그인 직후 진입점 결정에 사용.
 */
data class AuthStatus(
    val personalInfoCompleted: Boolean = false,
    val groupOnboardingCompleted: Boolean = false,
    val mainAccessible: Boolean = false,
)
