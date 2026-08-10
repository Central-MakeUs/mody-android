package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.HealthAvailability
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
     * **직전** 주간 챌린지 조회가 성공했는지. "한 번이라도"가 아니다.
     *
     * 조회 실패도 빈 목록이라, "없습니다" 문구는 성공 응답이 실제로 비었을 때만 띄운다.
     * 누적 플래그로 두면 처음에 빈 응답을 받은 뒤 재조회가 실패했을 때도 계속 "없습니다"가
     * 떠서, 모르는 상태를 없다고 단정하게 된다.
     *
     * 실패해도 직전에 받아둔 목록이 있으면 화면은 그걸 계속 보여준다(섹션은 목록이 비어
     * 있을 때만 숨는다) — 일시적 실패로 내용이 사라지지 않게.
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
    /**
     * 권한 요청이 거부로 끝나 안내가 필요한 상태.
     *
     * 시스템 요청은 두 번 거부되면 이후로는 다이얼로그 없이 즉시 거부로 돌아온다. 그때
     * 아무것도 안 하면 새로고침을 눌러도 화면이 그대로라 사용자는 기능이 고장 났다고 본다.
     * 남은 경로는 Health Connect 설정뿐이라 그쪽으로 안내한다.
     */
    val showHealthPermissionGuide: Boolean = false,
    /**
     * 값이 있으면 이 가용성에 맞는 Health Connect 설정 화면을 연다(일회성).
     * 실행 후 [ChallengeIntent.HealthSettingsLaunched] 로 비운다.
     */
    val healthSettingsRequest: HealthAvailability? = null,
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
