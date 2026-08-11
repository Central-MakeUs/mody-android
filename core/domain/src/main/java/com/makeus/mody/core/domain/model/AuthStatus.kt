package com.makeus.mody.core.domain.model

/**
 * 로그인 응답이 내려주는 진행 상태 flag.
 * 앱 시작/로그인 직후 진입점 결정에 사용 — [com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase].
 *
 * 서버는 groupOnboardingCompleted 도 함께 내려주지만 진입 분기에 쓰이지 않아 담지 않는다
 * (그룹을 다 나가면 mainAccessible=false 로 충분하다). 필요해지면 DTO 는
 * ignoreUnknownKeys 라 필드만 되살리면 된다.
 */
data class AuthStatus(
    val personalInfoCompleted: Boolean = false,
    val mainAccessible: Boolean = false,
)
