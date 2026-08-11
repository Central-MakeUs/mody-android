package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.SocialLoginType

/**
 * 소셜 로그인/토큰 관리.
 * 구현체는 서버 API 호출 후 토큰+상태를 SessionRepository 에 저장한다.
 *
 * 로그인 계열은 진행 상태를 반환하지 않는다 — 저장된 세션을 읽는
 * [com.makeus.mody.core.domain.usecase.ResolveStartDestinationUseCase] 가 진입 화면의
 * 유일한 판단자이고, 반환값을 따로 쓰면 같은 규칙이 두 벌 생긴다.
 */
interface AuthRepository {
    /** 소셜 SDK 에서 받은 accessToken 으로 서버 로그인. 성공 시 토큰/상태를 저장한다. */
    suspend fun loginWithSocial(type: SocialLoginType, socialAccessToken: String)

    /**
     * 스토어 심사용 히든 로그인. 서버 데모 provider 로 소셜 SDK 없이 로그인한다.
     * 성공 시 토큰/상태를 저장한다(소셜 로그인과 동일 흐름).
     */
    suspend fun loginForReview()

    /** 로그아웃(서버 통지 + 로컬 세션 초기화). */
    suspend fun logout()

    /** 회원탈퇴(서버 계정 삭제 + 로컬 세션 초기화). 되돌릴 수 없음. */
    suspend fun withdraw()
}
