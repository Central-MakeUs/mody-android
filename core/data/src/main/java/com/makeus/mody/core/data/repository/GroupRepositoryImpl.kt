package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.Group
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.network.api.GroupApi
import com.makeus.mody.core.network.model.group.CreateGroupRequest
import com.makeus.mody.core.network.model.group.GroupResponse
import com.makeus.mody.core.network.model.group.JoinGroupRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val sessionRepository: SessionRepository,
) : GroupRepository {

    override suspend fun createGroup(name: String): Group {
        val response = groupApi.createGroup(CreateGroupRequest(name)).unwrapResult()
        markGroupJoined()
        return response.toGroup()
    }

    override suspend fun joinGroup(code: String): Group {
        val response = groupApi.joinGroup(JoinGroupRequest(code)).unwrapResult()
        markGroupJoined()
        return response.toGroup()
    }

    override suspend fun leaveGroup(groupId: Long) {
        groupApi.leaveGroup(groupId).unwrapResult()
    }

    override suspend fun getMyGroups(): List<Group> =
        groupApi.getMyGroups().unwrapResult().groups.map { summary ->
            Group(
                groupId = summary.groupId,
                code = summary.code,
                name = summary.name,
                memberCount = summary.memberCount,
            )
        }

    // 그룹 보유 → 재접속 시 시작 라우팅이 MAIN 으로 가도록 세션 갱신.
    // updateStatus 로 personalInfoCompleted 를 건드리지 않고 이 플래그만 원자적으로 바꾼다.
    private suspend fun markGroupJoined() {
        sessionRepository.updateStatus { copy(mainAccessible = true) }
    }

    override suspend fun markNoGroups() {
        sessionRepository.updateStatus { copy(mainAccessible = false) }
    }
}

private fun GroupResponse.toGroup(): Group =
    Group(groupId = groupId, code = code, name = name, memberCount = memberCount)
