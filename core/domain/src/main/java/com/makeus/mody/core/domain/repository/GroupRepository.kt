package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.Group

/**
 * 온보딩 그룹 생성/참여.
 * 구현체는 성공 시 세션 상태(mainAccessible/groupOnboardingCompleted)를 갱신한다.
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
}
