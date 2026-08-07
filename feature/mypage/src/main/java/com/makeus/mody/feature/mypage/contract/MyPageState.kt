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
    /**
     * 프로필 조회가 한 번이라도 성공했는지. 스켈레톤은 값이 아직 없는 첫 로드에만 띄운다
     * — 복귀 재조회마다 띄우면 이미 보이던 이름/아바타가 깜빡인다.
     */
    val isProfileLoaded: Boolean = false,
    /** 체중 요약 조회 성공 여부. 기록이 없어도 [weight] 가 null 일 수 있어 별도 플래그가 필요하다. */
    val isWeightLoaded: Boolean = false,
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
