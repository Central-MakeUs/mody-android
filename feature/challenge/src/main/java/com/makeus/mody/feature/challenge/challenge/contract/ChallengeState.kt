package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget

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
    val error: String? = null,
) : UiState
