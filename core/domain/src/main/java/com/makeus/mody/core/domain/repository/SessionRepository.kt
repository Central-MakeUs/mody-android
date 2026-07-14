package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.AuthStatus
import com.makeus.mody.core.domain.model.SocialLoginType

/**
 * 로그인/온보딩 세션 상태 저장소.
 * 토큰 + 진행 상태 flag 를 영속화해 앱 시작 라우팅에 사용.
 */
interface SessionRepository {
    /** 로그인 토큰이 유효하게 저장돼 있는가. */
    suspend fun isLoggedIn(): Boolean

    /** 로그인 성공 시 토큰 저장(영속). */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /** 저장된 refresh token(로그아웃/재발급용). 없으면 빈 문자열. */
    suspend fun getRefreshToken(): String

    /** 진행 상태 flag 저장(로그인/온보딩 진행에 따라 갱신). */
    suspend fun saveStatus(status: AuthStatus)

    /** 저장된 진행 상태 flag. 없으면 전부 false. */
    suspend fun getStatus(): AuthStatus

    /** 마지막 로그인 소셜 타입 저장(무음 재로그인 시 provider 선택용). */
    suspend fun saveLastLoginType(type: SocialLoginType)

    /** 마지막 로그인 소셜 타입. 로그인 이력 없으면 null. */
    suspend fun getLastLoginType(): SocialLoginType?

    /** 로그아웃 등 세션 초기화(토큰 + 상태 제거). */
    suspend fun clear()
}
