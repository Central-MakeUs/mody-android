package com.makeus.mody.feature.mypage.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.model.WeightSummary

data class MyPageState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val daysTogether: Int = 0,
    val weight: WeightSummary? = null,
    val isLoading: Boolean = false,
    /** 체중 기록 바텀시트 표시 여부. */
    val showWeightSheet: Boolean = false,
    /** 체중 기록 저장 중. */
    val isRecordingWeight: Boolean = false,
    /** 체중 기록 저장 실패 메시지(토스트 후 소비). */
    val weightError: String? = null,
    /** 상단바 알림 아이콘 뱃지(안 읽은 알림 존재). */
    val hasUnreadNotification: Boolean = false,
    /** Phase 2 기능 노출(Remote Config). Phase 1 에선 건강 데이터 연동 설정 메뉴 숨김. */
    val phaseTwoFeaturesEnabled: Boolean = false,
    /**
     * 건강 데이터 연동 설정 진입 요청(1회성, 실행 후 null).
     * 기기의 Health Connect 상태에 따라 목적지가 갈려(설정 화면 vs 스토어 설치) 화면이 인텐트를 만든다.
     */
    val healthSettingsRequest: HealthAvailability? = null,
) : UiState
