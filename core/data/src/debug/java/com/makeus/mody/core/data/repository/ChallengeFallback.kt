package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeOption
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.model.WeeklyChallengeParticipant

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

    /**
     * 달성치는 0 — 건강 데이터에서 읽은 실제 걸음 수가 곧바로 덮어쓴다.
     * 더미 달성치를 두면 게이지가 확 찼다가 실제 값으로 뚝 떨어지는 게 보인다.
     */
    val stepChallenge: StepChallengeStatus? = StepChallengeStatus(
        groupChallengeId = 1,
        title = "수원까지 걸어가기 챌린지",
        targetStepCount = 45_000,
        currentStepCount = 0,
    )

    val stepChallengeOptions: List<StepChallengeOption> = listOf(
        StepChallengeOption(1, "서울에서 인천까지 걸어가기", "서울", "인천", 60.0, 150_000, false),
        StepChallengeOption(2, "서울에서 천안까지 걸어가기", "서울", "천안", 90.0, 200_000, false),
        StepChallengeOption(3, "서울에서 대전까지 걸어가기", "서울", "대전", 160.0, 300_000, false),
        StepChallengeOption(4, "서울에서 대구까지 걸어가기", "서울", "대구", 240.0, 400_000, false),
        StepChallengeOption(5, "서울에서 부산까지 걸어가기", "서울", "부산", 325.0, 500_000, false),
        StepChallengeOption(6, "서울에서 제주까지 걸어가기", "서울", "제주", 500.0, 700_000, false),
    )

    val stepRankings: List<StepRanking> = listOf(
        StepRanking(rank = 1, memberId = 1, nickname = "나는야화영", profileImageUrl = null, stepCount = 15_000),
        StepRanking(rank = 2, memberId = 2, nickname = "예은", profileImageUrl = null, stepCount = 14_000),
        StepRanking(rank = 3, memberId = 3, nickname = "동준", profileImageUrl = null, stepCount = 13_000),
        StepRanking(rank = 4, memberId = 4, nickname = "도윤", profileImageUrl = null, stepCount = 10_000),
        StepRanking(rank = 5, memberId = 5, nickname = "민석", profileImageUrl = null, stepCount = 4_000),
    )

    /** 시안 그대로 5명 참여 — 앞 3명 아바타 + "+2" 가 나오는 상태. */
    private val weeklyParticipants: List<WeeklyChallengeParticipant> = (1..5).map {
        WeeklyChallengeParticipant(memberId = it.toLong(), nickname = "버디$it", profileImageUrl = null)
    }

    val weeklyChallenges: List<WeeklyChallenge> = listOf(
        WeeklyChallenge(
            groupChallengeId = 1,
            title = "이번주의 고해성사하기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
            participants = weeklyParticipants,
        ),
        WeeklyChallenge(
            groupChallengeId = 2,
            title = "하루에 줄넘기 15분 하기",
            deadlineDayOfWeek = "FRIDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
            participants = weeklyParticipants,
        ),
        WeeklyChallenge(
            groupChallengeId = 3,
            title = "엘레베이터 안 타고 올라가기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
            participants = weeklyParticipants,
        ),
        WeeklyChallenge(
            groupChallengeId = 4,
            title = "한 정거장 미리 내려 걸어가기",
            deadlineDayOfWeek = "SUNDAY",
            participantCount = 5,
            randomParticipantNickname = "버디1",
            participants = weeklyParticipants,
        ),
    )
}
