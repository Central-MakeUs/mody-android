package com.makeus.mody.core.domain.model

/** 마이페이지 상단 프로필. */
data class MyProfile(
    val nickname: String,
    val profileImageUrl: String?,
    /** 모디와 함께한 일수. */
    val daysTogether: Int,
)

/** 체중 요약(이전·현재·목표). 각 값은 미기록 시 null. */
data class WeightSummary(
    val startKg: Double?,
    val currentKg: Double?,
    val targetKg: Double?,
) {
    /** 하나라도 기록이 있으면 카드 노출. */
    val hasAny: Boolean get() = startKg != null || currentKg != null || targetKg != null
}
