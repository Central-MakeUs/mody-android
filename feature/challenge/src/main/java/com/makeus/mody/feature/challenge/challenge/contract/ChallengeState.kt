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
    /** 상단바 알림 아이콘 뱃지(안 읽은 알림 존재). */
    val hasUnreadNotification: Boolean = false,
    /** 연속 기록 탭 상단 요약. null 이면 로딩 전/실패. */
    val summary: ChallengeSummary? = null,
    /** 버디 신기록 도전 목록. */
    val buddies: List<NudgeTarget> = emptyList(),
    /**
     * 버디 목록 조회가 한 번이라도 성공했는지.
     *
     * 조회 실패도 빈 목록으로 보이기 때문에, "혼자인 그룹" 빈 화면은 성공 응답이
     * 실제로 비었을 때만 띄운다. 네트워크 오류로 빈 화면이 뜨는 것을 막는다.
     */
    val buddiesLoaded: Boolean = false,
    /**
     * 주간 챌린지를 한 번이라도 성공적으로 받아왔는지.
     * [buddiesLoaded] 와 같은 이유 — 조회 실패도 빈 목록이라, "이번주 챌린지가 없어요"는
     * 성공 응답이 실제로 비었을 때만 띄운다. 네트워크 오류를 "없음"으로 단정하지 않는다.
     */
    val weeklyLoaded: Boolean = false,
    /**
     * 콕 찌르기 전송 중인 memberId — 중복 탭 방지.
     * "이미 찔렀는지"는 서버가 [NudgeTarget.nudgeStatus] 로 주므로 여기서 들지 않는다.
     */
    val nudgingMemberIds: Set<Long> = emptySet(),
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
    /** 현재 그룹 인원(나 포함). null/0 이면 아직 못 받았다. */
    val groupMemberCount: Int? = null,
    val error: String? = null,
) : UiState {

    /**
     * 나 혼자인 그룹 — 연속 기록도 챌린지도 성립하지 않아 두 탭 모두 막는다.
     *
     * 그룹 목록의 인원 수가 1차 기준. 못 받았을 때(조회 실패/서버 미제공)만 버디 목록으로
     * 대체 판정하되, 조회 실패도 빈 목록이라 성공한 적이 있을 때만 인정한다.
     */
    val isSoloGroup: Boolean
        get() = if (groupMemberCount != null && groupMemberCount > 0) {
            groupMemberCount <= 1
        } else {
            buddiesLoaded && buddies.isEmpty()
        }
}
