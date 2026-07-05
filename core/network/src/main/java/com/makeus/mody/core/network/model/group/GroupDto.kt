package com.makeus.mody.core.network.model.group

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class JoinGroupRequest(
    val code: String,
)

/** 그룹 생성/참여 공통 응답. memberCount 는 참여 응답에만 존재. */
@Serializable
data class GroupResponse(
    val groupId: Long = 0,
    val code: String = "",
    val name: String = "",
    val memberCount: Int = 0,
)
