package com.makeus.mody.feature.mypage.notification.contract

import com.makeus.mody.core.commonui.base.UiState

data class NotificationSettingState(
    /** 식사 및 운동 알림(끄면 식사/운동 스케줄 편집 숨김). */
    val recordReminderEnabled: Boolean = false,
    val commentEnabled: Boolean = false,
    val challengeEnabled: Boolean = false,
    /** 챌린지 기능 노출(Remote Config). Phase 1 에선 챌린지 알림 토글 행 자체를 숨김. */
    val phaseTwoFeaturesEnabled: Boolean = false,
    /**
     * 응원 댓글 기능 노출(Remote Config `comment_flag`). 닫히면 "코멘트 알림" 토글 행을 숨긴다.
     *
     * 위 [commentEnabled] 와 다르다 — 저쪽은 사용자가 켜둔 알림 수신 여부이고,
     * 이건 기능 자체를 보여줄지다.
     */
    val commentFeatureEnabled: Boolean = false,
    // 식사 시각(null = 식사 안 함).
    val breakfastHour: Int? = 8,
    val lunchHour: Int? = 12,
    val dinnerHour: Int? = 18,
    /** 선택된 운동요일(1=월~7=일) → (hour24, minute). */
    val exerciseTimes: Map<Int, Pair<Int, Int>> = emptyMap(),
    val isLoading: Boolean = true,
    /**
     * 서버(또는 캐시)에서 설정을 실제로 읽었는지. 위 토글 기본값 false 는 "꺼짐"이 아니라
     * "아직 모름"이라, 이 값이 false 인 상태로 PATCH 하면 안 읽은 값이 서버에 확정 저장된다
     * (서버 PATCH 가 전체 교체라 항상 토글 3개를 함께 보내기 때문).
     */
    val isLoaded: Boolean = false,
    val error: String? = null,
) : UiState
