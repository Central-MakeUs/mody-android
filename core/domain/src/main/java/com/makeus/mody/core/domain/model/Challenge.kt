package com.makeus.mody.core.domain.model

import java.time.Instant

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
    /**
     * 이 챌린지의 걸음 수를 세기 시작하는 시각. 챌린지를 바꾼 날은 리셋 시각이라
     * 그날 0시부터 세면 리셋 전 걸음이 섞인다. null 이면 오늘 0시부터로 본다.
     */
    val fetchFromAt: Instant? = null,
) {
    /** 달성률(0~100). 목표 0이면 0. */
    val progressPercent: Int
        get() = if (targetStepCount <= 0) 0
        else (currentStepCount * 100 / targetStepCount).coerceIn(0, 100)
}

/**
 * 챌린지 변경 화면에서 고를 수 있는 걸음 수 챌린지 한 줄.
 * @param selected 현재 그룹이 진행 중인 챌린지.
 */
data class StepChallengeOption(
    val challengeId: Long,
    val title: String,
    val departure: String,
    val destination: String,
    val distanceKm: Double,
    val targetStepCount: Int,
    val selected: Boolean,
)

/** 걸음 수 기여도 순위 한 줄. */
data class StepRanking(
    val rank: Int,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val stepCount: Int,
)

/** 주간 챌린지 인증 사진 한 장. */
data class WeeklyChallengeProof(
    val proofId: Long,
    val imageUrl: String,
    /** 서버가 원본을 저장하고 표시 영역만 내려준다. null 이면 원본 전체. */
    val cropRegion: CropRegion?,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
)

/** 공유용 콜라주 이미지. 서버가 인증 사진들을 합쳐 한 장으로 만들어 준다. */
data class WeeklyChallengeShare(
    val imageUrl: String,
    val cropRegion: CropRegion?,
    val rows: Int,
    val columns: Int,
)

/** 주간 챌린지 참여자 — 카드의 겹침 아바타에 쓴다. */
data class WeeklyChallengeParticipant(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
)

/** 그룹 선택(주간) 챌린지 한 줄. */
data class WeeklyChallenge(
    val groupChallengeId: Long,
    val title: String,
    /** 마감 요일 — 서버 enum 문자열(MONDAY~SUNDAY). D-day 계산은 표시 계층에서. */
    val deadlineDayOfWeek: String,
    val participantCount: Int,
    val randomParticipantNickname: String?,
    /** 참여자 목록. [participantCount] 보다 짧을 수 있어 "+N" 은 count 기준으로 센다. */
    val participants: List<WeeklyChallengeParticipant> = emptyList(),
)
