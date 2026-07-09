package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType

/**
 * 소셜 로그인/토큰 관리.
 * 구현체는 서버 API 호출 후 토큰+상태를 SessionRepository 에 저장한다.
 */
interface AuthRepository {
    /**
     * 소셜 SDK 에서 받은 accessToken 으로 서버 로그인.
     * 성공 시 토큰/상태를 저장하고 진행 상태를 반환한다.
     */
    suspend fun loginWithSocial(type: SocialLoginType, socialAccessToken: String): AuthStatus

    /** 로그아웃(서버 통지 + 로컬 세션 초기화). */
    suspend fun logout()

    /** 회원탈퇴(서버 계정 삭제 + 로컬 세션 초기화). 되돌릴 수 없음. */
    suspend fun withdraw()
}
