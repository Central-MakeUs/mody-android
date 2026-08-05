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
    /**
     * 오늘 이미 콕 찌른 memberId. 서버가 하루 1회 제한을 걸지만 "이미 찔렀는지"를
     * 응답으로 주지 않아 기기에 남긴 기록으로 버튼 상태를 복원한다.
     */
    val nudgedMemberIds: Set<Long> = emptySet(),
    /** 일회성 안내 문구(토스트). 표시 후 [ChallengeIntent.ToastShown] 으로 비운다. */
    val toastMessage: String? = null,
    /** 그룹 필수(걸음 수) 챌린지 현황. null 이면 진행 중 챌린지 없음/로딩 전. */
    val stepChallenge: StepChallengeStatus? = null,
    /** 걸음 수 기여도 순위. */
    val stepRankings: List<StepRanking> = emptyList(),
    /** 이번주 그룹 선택(주간) 챌린지 목록. */
    val weeklyChallenges: List<WeeklyChallenge> = emptyList(),
    /** 걸음 수 동기화(건강 데이터 읽기 + 서버 반영) 진행 중. */
    val isSyncingSteps: Boolean = false,
    /**
     * 값이 있으면 이 권한들로 시스템 요청을 띄운다(일회성).
     * 런처 실행 후 [ChallengeIntent.HealthPermissionRequestLaunched] 로 비운다.
     */
    val healthPermissionRequest: Set<String>? = null,
    val error: String? = null,
) : UiState
