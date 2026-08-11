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

    /** 진행 상태 flag 전체 교체. 서버 응답이 확정값을 줄 때만 쓴다(로그인 응답 등). */
    suspend fun saveStatus(status: AuthStatus)

    /**
     * 진행 상태 flag 부분 갱신. 읽기-수정-쓰기를 **하나의 트랜잭션**으로 처리한다.
     *
     * `saveStatus(getStatus().copy(...))` 로 나눠 쓰면 두 호출 사이에 다른 쪽이 쓴 값을
     * 덮어쓴다 — 예: 그룹 참여(mainAccessible=true)와 그룹 없음 감지(false)가 겹치면
     * 나중에 쓴 쪽이 이긴다. [transform] 은 재시도 시 다시 실행될 수 있으므로 순수해야 한다.
     */
    suspend fun updateStatus(transform: AuthStatus.() -> AuthStatus)

    /** 저장된 진행 상태 flag. 없으면 전부 false. */
    suspend fun getStatus(): AuthStatus

    /** 마지막 로그인 소셜 타입 저장(무음 재로그인 시 provider 선택용). */
    suspend fun saveLastLoginType(type: SocialLoginType)

    /** 마지막 로그인 소셜 타입. 로그인 이력 없으면 null. */
    suspend fun getLastLoginType(): SocialLoginType?

    /** 피드에서 마지막으로 보던 그룹 저장(앱 재진입 시 복원용). */
    suspend fun saveLastGroupId(groupId: Long)

    /** 피드에서 마지막으로 보던 그룹 id. 이력 없으면 null. */
    suspend fun getLastGroupId(): Long?

    /** 건강 데이터 권한을 이미 한 번 요청했는지 기록(챌린지 탭 재진입마다 팝업 방지). */
    suspend fun saveHealthPermissionAsked()

    /** 건강 데이터 권한을 요청한 이력이 있는지. */
    suspend fun getHealthPermissionAsked(): Boolean

    /** 로그아웃 등 세션 초기화(토큰 + 상태 제거). */
    suspend fun clear()
}
