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

    /** 피드에서 마지막으로 보던 그룹 저장(앱 재진입 시 복원용). */
    suspend fun saveLastGroupId(groupId: Long)

    /** 피드에서 마지막으로 보던 그룹 id. 이력 없으면 null. */
    suspend fun getLastGroupId(): Long?

    /**
     * 오늘 콕 찌른 멤버 기록. 서버가 하루 1회 제한을 걸지만 "이미 찔렀는지"를
     * 응답으로 주지 않아, 버튼 상태를 되살리려면 기기에 남겨야 한다.
     * @param today yyyy-MM-dd. 저장된 날짜와 다르면 이전 기록은 버려진다.
     */
    suspend fun saveNudgedMember(groupId: Long, memberId: Long, today: String)

    /** [today] 에 이 그룹에서 콕 찌른 멤버 id. 날짜가 바뀌었으면 빈 집합. */
    suspend fun getNudgedMembers(groupId: Long, today: String): Set<Long>

    /** 건강 데이터 권한을 이미 한 번 요청했는지 기록(챌린지 탭 재진입마다 팝업 방지). */
    suspend fun saveHealthPermissionAsked()

    /** 건강 데이터 권한을 요청한 이력이 있는지. */
    suspend fun getHealthPermissionAsked(): Boolean

    /** 로그아웃 등 세션 초기화(토큰 + 상태 제거). */
    suspend fun clear()
}
