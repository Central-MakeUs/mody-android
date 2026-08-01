package com.makeus.mody.core.domain.model

/** 챌린지 탭 상단 요약 — 그룹 연속 기록/통계. */
data class ChallengeSummary(
    /** 그룹과 함께한 일수 (D+N). */
    val daysTogether: Int,
    /** 전원 연속 기록 일수 (N일째). */
    val allMemberRecordedDays: Int,
    /** 이번달 함께한 운동시간(분). */
    val monthlyExerciseMinutes: Int,
    /** 이번달 함께한 챌린지 개수. */
    val monthlyCompletedChallengeCount: Int,
)

/** 버디 신기록 도전 한 줄 — 오늘 기록 여부 + 콕 찌르기 대상. */
data class NudgeTarget(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val recordedToday: Boolean,
)

/** 그룹 필수(걸음 수) 챌린지 현황. */
data class StepChallengeStatus(
    val groupChallengeId: Long,
    val title: String,
    val targetStepCount: Int,
    val currentStepCount: Int,
) {
    /** 달성률(0~100). 목표 0이면 0. */
    val progressPercent: Int
        get() = if (targetStepCount <= 0) 0
        else (currentStepCount * 100 / targetStepCount).coerceIn(0, 100)
}

/** 걸음 수 기여도 순위 한 줄. */
data class StepRanking(
    val rank: Int,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val stepCount: Int,
)

/** 그룹 선택(주간) 챌린지 한 줄. */
data class WeeklyChallenge(
    val groupChallengeId: Long,
    val title: String,
    /** 마감 요일 — 서버 enum 문자열(MONDAY~SUNDAY). D-day 계산은 표시 계층에서. */
    val deadlineDayOfWeek: String,
    val participantCount: Int,
    val randomParticipantNickname: String?,
)
