package com.makeus.mody.core.domain.model

/**
 * 그룹. 생성/참여 응답 단위.
 * memberCount 는 참여 응답에만 실려온다(생성 시 0).
 */
data class Group(
    val groupId: Long,
    val code: String,
    val name: String,
    val memberCount: Int = 0,
)
