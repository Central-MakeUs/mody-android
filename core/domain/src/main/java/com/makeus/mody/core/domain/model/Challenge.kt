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
