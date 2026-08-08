package com.makeus.mody.feature.challenge.weeklydetail.contract

import com.makeus.mody.core.commonui.base.UiState
import com.makeus.mody.core.domain.model.WeeklyChallengeProof

data class WeeklyChallengeDetailState(
    val title: String = "",
    /**
     * 안내 문구("집까지 계단으로 이동한 사진을 인증해주세요!").
     * 상세 API 가 목록에 없는 challengeId 를 요구해 아직 못 받아온다 — 비어 있으면 줄 자체를 그리지 않는다.
     */
    val description: String = "",
    /** 서버 enum 문자열(MONDAY~SUNDAY). D-day 표시용. */
    val deadlineDayOfWeek: String = "",
    val isLoading: Boolean = true,
    val proofs: List<WeeklyChallengeProof> = emptyList(),
    /** 촬영 오버레이 표시 중. */
    val isCameraVisible: Boolean = false,
    /** 업로드 진행 중 — 중복 등록 방지 + 인디케이터. */
    val isUploading: Boolean = false,
    /** 공유 이미지 준비 중(콜라주 생성 + 다운로드). */
    val isPreparingShare: Boolean = false,
    /** 공유 시트에 넘길 로컬 이미지 URI. 소비하면 null 로 되돌린다. */
    val shareImageUri: String? = null,
    val toastMessage: String? = null,
    val error: String? = null,
) : UiState
