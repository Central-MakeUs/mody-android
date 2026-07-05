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

    // 그룹 보유 → 재접속 시 시작 라우팅이 MAIN 으로 가도록 세션 갱신
    private suspend fun markGroupJoined() {
        sessionRepository.saveStatus(
            sessionRepository.getStatus().copy(
                groupOnboardingCompleted = true,
                mainAccessible = true,
            ),
        )
    }
}

private fun GroupResponse.toGroup(): Group =
    Group(groupId = groupId, code = code, name = name, memberCount = memberCount)
