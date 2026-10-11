package com.makeus.mody.feature.challenge.challenge.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class ChallengeIntent : UiIntent {
    /** 탭 진입/복귀 시 재조회. */
    data object ScreenEntered : ChallengeIntent()

    /** 상단 서브탭(연속 기록/챌린지) 전환. */
    data class SubTabSelected(val tab: ChallengeSubTab) : ChallengeIntent()

    /** 상단 알림 아이콘. */
    data object AlarmClicked : ChallengeIntent()

    /** 버디 콕 찌르기. */
    data class NudgeClicked(val memberId: Long) : ChallengeIntent()

    /** 걸음 수 새로고침(달성 걸음수 옆 리셋 아이콘). */
    data object StepRefreshClicked : ChallengeIntent()

    /** 걸음 수 챌린지 변경. */
    data object ChangeStepChallengeClicked : ChallengeIntent()

    /** 주간 챌린지 항목 → 상세. */
    data class WeeklyChallengeClicked(val groupChallengeId: Long) : ChallengeIntent()

    /** 건강 데이터 권한 요청 결과. */
    data class HealthPermissionResult(val granted: Boolean) : ChallengeIntent()

    /** 권한 요청 런처를 띄운 뒤 일회성 트리거 해제. */
    data object HealthPermissionRequestLaunched : ChallengeIntent()

    /** 권한 거부 안내에서 "설정 열기". */
    data object HealthSettingsClicked : ChallengeIntent()

    /** 설정 화면을 띄운 뒤 일회성 트리거 해제. */
    data object HealthSettingsLaunched : ChallengeIntent()

    /** 권한 거부 안내 닫기. */
    data object HealthPermissionGuideDismissed : ChallengeIntent()

    /** 토스트 표시 완료 → 일회성 문구 해제. */
    data object ToastShown : ChallengeIntent()

    /** 에러 다이얼로그 확인. */
    data object ErrorShown : ChallengeIntent()
}
