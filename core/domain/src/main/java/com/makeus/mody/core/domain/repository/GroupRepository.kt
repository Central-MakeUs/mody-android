package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.Group

/**
 * 온보딩 그룹 생성/참여.
 * 세션의 mainAccessible 갱신은 구현체가 책임진다 — 화면이 직접 세션을 만지지 않는다.
 */
interface GroupRepository {
    /** 그룹 생성. 응답의 초대 코드를 담은 Group 반환. */
    suspend fun createGroup(name: String): Group

    /** 초대 코드로 그룹 참여. */
    suspend fun joinGroup(code: String): Group

    /** 내가 속한 그룹 목록. */
    suspend fun getMyGroups(): List<Group>

    /** 그룹 나가기. */
    suspend fun leaveGroup(groupId: Long)

    /**
     * 속한 그룹이 하나도 없음을 세션에 반영한다(재접속 시 시작 라우팅이 GROUP 으로 가도록).
     * 다른 기기에서 전부 나간 경우처럼 화면이 빈 목록을 확인했을 때 호출한다.
     */
    suspend fun markNoGroups()
}
