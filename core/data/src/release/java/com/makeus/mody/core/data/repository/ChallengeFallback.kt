package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge

/**
 * release 빌드용 빈 폴백 — 더미 데이터 없음.
 *
 * debug 소스셋의 동명 오브젝트와 시그니처만 맞춰, [ChallengeRepositoryImpl] 이
 * 변형별 분기 없이 같은 코드를 쓰도록 한다. 배포 빌드에서는 API 실패가 그대로 예외로 전파된다.
 *
 * TODO(challenge): 서버에 챌린지 데이터가 채워지면 debug/release 폴백 파일 모두 제거.
 */
internal object ChallengeFallback {

    const val ENABLED = false

    val summary: ChallengeSummary? = null

    val nudgeTargets: List<NudgeTarget> = emptyList()

    val stepChallenge: StepChallengeStatus? = null

    val stepRankings: List<StepRanking> = emptyList()

    val weeklyChallenges: List<WeeklyChallenge> = emptyList()
}
