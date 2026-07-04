package com.makeus.mody.core.domain.model

/**
 * 앱 시작 시 진입할 최상위 목적지(도메인 표현).
 * 네비게이션 Route 는 모른다 — presentation 에서 Route 로 매핑한다.
 */
enum class StartDestination {
    /** 미인증 → 로그인. */
    AUTH,

    /** 인증됐으나 프로필(온보딩) 미완료 → 온보딩. */
    ONBOARDING,

    /** 온보딩 완료, 그룹 온보딩 미완료 → 그룹 플로우. */
    GROUP,

    /** 전부 완료 → 메인. */
    MAIN,
}
