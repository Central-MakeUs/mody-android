package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge

/** 챌린지 메인 상단 서브탭. */
enum class ChallengeSubTab(val label: String) {
    STREAK("연속 기록"),
    CHALLENGE("챌린지"),
}

data class ChallengeState(
    val selectedSubTab: ChallengeSubTab = ChallengeSubTab.STREAK,
    val isLoading: Boolean = false,
    /** 연속 기록 탭 상단 요약. null 이면 로딩 전/실패. */
    val summary: ChallengeSummary? = null,
    /** 버디 신기록 도전 목록. */
    val buddies: List<NudgeTarget> = emptyList(),
    /** 콕 찌르기 전송 중인 memberId — 중복 탭 방지. */
    val nudgingMemberIds: Set<Long> = emptySet(),
    /** 그룹 필수(걸음 수) 챌린지 현황. null 이면 진행 중 챌린지 없음/로딩 전. */
    val stepChallenge: StepChallengeStatus? = null,
    /** 걸음 수 기여도 순위. */
    val stepRankings: List<StepRanking> = emptyList(),
    /** 이번주 그룹 선택(주간) 챌린지 목록. */
    val weeklyChallenges: List<WeeklyChallenge> = emptyList(),
    val error: String? = null,
) : UiState
