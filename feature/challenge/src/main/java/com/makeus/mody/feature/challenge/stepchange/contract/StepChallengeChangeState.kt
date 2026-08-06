package com.makeus.mody.feature.challenge.stepchange.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.StepChallengeOption

data class StepChallengeChangeState(
    val isLoading: Boolean = true,
    /** 선택 가능한 걸음 수 챌린지 목록. */
    val options: List<StepChallengeOption> = emptyList(),
    /**
     * 확인 다이얼로그에 걸린 챌린지. 교체하면 기존 걸음 기록이 사라지므로
     * 탭 즉시 반영하지 않고 한 번 되묻는다.
     */
    val pendingOption: StepChallengeOption? = null,
    /** 교체 요청 전송 중 — 다이얼로그 확정 버튼 중복 탭 방지. */
    val isChanging: Boolean = false,
    val error: String? = null,
) : UiState
