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

/** GET /api/v1/groups/{groupId}/challenges/step/current — 걸음 수 챌린지 현황. */
@Serializable
data class StepChallengeStatusResponse(
    val groupChallengeId: Long = 0,
    val title: String = "",
    val targetStepCount: Int = 0,
    val currentStepCount: Int = 0,
)

/** GET /api/v1/groups/{groupId}/challenges/step/rankings — 기여도 순위. */
@Serializable
data class StepRankingListResponse(
    val rankings: List<StepRankingResponse> = emptyList(),
)

@Serializable
data class StepRankingResponse(
    val rank: Int = 0,
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val stepCount: Int = 0,
)

/** GET /api/v1/groups/{groupId}/challenges/weekly — 주간 챌린지 목록. */
@Serializable
data class WeeklyChallengeListResponse(
    val challenges: List<WeeklyChallengeSummaryResponse> = emptyList(),
)

@Serializable
data class WeeklyChallengeSummaryResponse(
    val groupChallengeId: Long = 0,
    val title: String = "",
    val deadlineDayOfWeek: String = "",
    val participantCount: Int = 0,
    val randomParticipantNickname: String? = null,
)
