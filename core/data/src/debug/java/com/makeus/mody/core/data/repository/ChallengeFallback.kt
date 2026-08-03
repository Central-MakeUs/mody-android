package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge

/**
 * debug 전용 챌린지 더미 데이터.
 *
 * 서버에 진행 중인 챌린지가 없어 UI(걸음 수 게이지·기여도 순위·주간 챌린지)를 확인할 수 없는 동안,
 * API 실패/빈 응답일 때만 시안 값으로 대체한다. 성공 응답이 있으면 항상 실제 데이터가 우선.
 *
 * release 소스셋(`src/release/.../ChallengeFallback.kt`)에는 빈 구현만 있어
 * 배포 빌드에는 이 문자열/숫자가 포함되지 않는다.
 *
 * TODO(challenge): 서버에 챌린지 데이터가 채워지면 debug/release 폴백 파일 모두 제거.
 */
internal object ChallengeFallback {

    /** 폴백 사용 여부. debug 에서만 true. */
    const val ENABLED = true

    // 타입은 release 폴백과 동일하게 nullable 로 선언 — Impl 이 `?: throw` 를 그대로 쓸 수 있게.
    val summary: ChallengeSummary? = ChallengeSummary(
        daysTogether = 30,
        allMemberRecordedDays = 23,
        monthlyExerciseMinutes = 1800,
        monthlyCompletedChallengeCount = 8,
    )

    val nudgeTargets: List<NudgeTarget> = listOf(
        NudgeTarget(memberId = 1, nickname = "예은", profileImageUrl = null, recordedToday = true),
        NudgeTarget(memberId = 2, nickname = "동준", profileImageUrl = null, recordedToday = false),
        NudgeTarget(memberId = 3, nickname = "도윤", profileImageUrl = null, recordedToday = true),
    )

    val stepChallenge: StepChallengeStatus? = StepChallengeStatus(
        groupChallengeId = 1,
        title = "수원까지 걸어가기 챌린지",
        targetStepCount = 45_000,
        currentStepCount = 38_000,
    )

    val stepRankings: List<StepRanking> = listOf(
        StepRanking(rank = 1, memberId = 1, nickname = "나는야화영", profileImageUrl = null, stepCount = 15_000),
        StepRanking(rank = 2, memberId = 2, nickname = "예은", profileImageUrl = null, stepCount = 14_000),
        StepRanking(rank = 3, memberId = 3, nickname = "동준", profileImageUrl = null, stepCount = 13_000),
        StepRanking(rank = 4, memberId = 4, nickname = "도윤", profileImageUrl = null, stepCount = 10_000),
        StepRanking(rank = 5, memberId = 5, nickname = "민석", profileImageUrl = null, stepCount = 4_000),
    )

    val weeklyChallenges: List<WeeklyChallenge> = listOf(
        WeeklyChallenge(
            groupChallengeId = 1,
            title = "이번주의 고해성사하기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
        ),
        WeeklyChallenge(
            groupChallengeId = 2,
            title = "하루에 줄넘기 15분 하기",
            deadlineDayOfWeek = "FRIDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
        ),
        WeeklyChallenge(
            groupChallengeId = 3,
            title = "엘레베이터 안 타고 올라가기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
        ),
        WeeklyChallenge(
            groupChallengeId = 4,
            title = "한 정거장 미리 내려 걸어가기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
        ),
    )
}
