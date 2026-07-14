package com.makeus.mody.core.domain.repository

/**
 * refresh 토큰까지 만료됐을 때 소셜 SDK에 남아있는 세션으로 무음 재로그인하는 추상화.
 * 구현은 feature:auth (소셜 SDK 접근 필요). 성공 시 새 JWT가 저장된 상태로 true 를 반환한다.
 */
interface SessionReauthenticator {
    suspend fun reauthenticate(): Boolean
}
