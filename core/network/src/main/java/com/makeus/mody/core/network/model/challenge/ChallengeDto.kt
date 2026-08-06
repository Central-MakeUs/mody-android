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
    /**
     * 이 챌린지의 걸음 수를 세기 시작할 시각. 챌린지를 바꾼 날은 리셋 시각(예: 14:40)이라
     * 그날 0시부터 세면 리셋 전 걸음이 새 챌린지에 섞인다.
     *
     * 서버 필드명이 확정 전이라 두 후보를 모두 받는다. 확정되면 하나만 남길 것.
     * TODO(challenge): 서버 필드명 확정 후 정리.
     */
    val stepCountFetchFromAt: String? = null,
    val startedAt: String? = null,
) {
    val fetchFromAt: String? get() = stepCountFetchFromAt ?: startedAt
}

/** PUT /api/v1/groups/{groupId}/challenges/step/records — 하루치 걸음 수 upsert. */
@Serializable
data class StepRecordUpsertRequest(
    /** yyyy-MM-dd */
    val recordedOn: String,
    val stepCount: Int,
)

@Serializable
data class StepRecordUpsertResponse(
    val groupChallengeId: Long = 0,
    val recordedOn: String = "",
    val stepCount: Int = 0,
    /** 그룹 누적(서버 재계산). */
    val currentStepCount: Int = 0,
    val targetStepCount: Int = 0,
    val completed: Boolean = false,
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

/** GET /api/v1/groups/{groupId}/challenges/step/options — 선택 가능한 걸음 수 챌린지 목록. */
@Serializable
data class StepChallengeOptionListResponse(
    val options: List<StepChallengeOptionResponse> = emptyList(),
)

@Serializable
data class StepChallengeOptionResponse(
    val challengeId: Long = 0,
    val title: String = "",
    val departure: String = "",
    val destination: String = "",
    val distanceKm: Double = 0.0,
    val targetStepCount: Int = 0,
    /** 현재 그룹이 진행 중인 챌린지인지. */
    val selected: Boolean = false,
)

/** PATCH /api/v1/groups/{groupId}/challenges/step/current — 진행 챌린지 교체. */
@Serializable
data class StepChallengeChangeRequest(
    val challengeId: Long,
)

@Serializable
data class StepChallengeChangeResponse(
    val groupChallengeId: Long = 0,
    val challengeId: Long = 0,
    val title: String = "",
    val targetStepCount: Int = 0,
    val currentStepCount: Int = 0,
    /** 교체 직후의 카운트 시작 시각. [StepChallengeStatusResponse.fetchFromAt] 과 동일 규칙. */
    val stepCountFetchFromAt: String? = null,
    val startedAt: String? = null,
) {
    val fetchFromAt: String? get() = stepCountFetchFromAt ?: startedAt
}

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
    /** 참여자 목록. 카드의 겹침 아바타에 쓴다. participantCount 보다 짧을 수 있다. */
    val participants: List<WeeklyChallengeParticipantResponse> = emptyList(),
)

@Serializable
data class WeeklyChallengeParticipantResponse(
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
)
