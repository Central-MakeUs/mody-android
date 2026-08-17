package com.makeus.mody.feature.mypage.healthguide.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.HealthAvailability

data class HealthGuideState(
    /** 현재 보고 있는 안내 단계(0-based). 페이저 위치와 인디케이터가 함께 본다. */
    val currentStep: Int = 0,
    /**
     * 설정 화면으로 보낼 준비가 된 상태. 화면이 소비하면 null 로 되돌린다.
     *
     * 목적지가 기기 상태에 따라 갈려(설치됨/업데이트 필요/미설치) 인텐트를 화면이 만든다.
     * ViewModel 은 안드로이드 Intent 를 모른다.
     */
    val settingsRequest: HealthAvailability? = null,
) : UiState
