package com.makeus.mody.core.network.model.challenge

import kotlinx.serialization.Serializable

/** GET /api/v1/groups/{groupId}/challenges/summary — 연속 기록 탭 상단 요약. */
@Serializable
data class ChallengeSummaryResponse(
    val daysTogether: Int = 0,
    val allMemberRecordedDays: Int = 0,
    val monthlyExerciseMinutes: Int = 0,
    val monthlyCompletedChallengeCount: Int = 0,
)

/** GET /api/v1/groups/{groupId}/challenges/nudges — 버디 신기록 목록. */
@Serializable
data class NudgeTargetListResponse(
    val members: List<NudgeTargetResponse> = emptyList(),
)

@Serializable
data class NudgeTargetResponse(
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val recordedToday: Boolean = false,
)
