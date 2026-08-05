package com.makeus.mody.core.domain.model

/** 기기의 건강 데이터(Health Connect) 사용 가능 여부. */
enum class HealthAvailability {
    /** 바로 사용 가능. */
    AVAILABLE,

    /** 제공자 앱 설치/업데이트가 필요(Android 13 이하 등). */
    UPDATE_REQUIRED,

    /** 이 기기에서는 지원하지 않음. */
    UNAVAILABLE,
}

/** 오늘 걸음 수를 서버에 올린 결과. */
data class StepRecordResult(
    val groupChallengeId: Long,
    /** 그룹 전체 누적 걸음 수(서버 재계산 값). */
    val currentStepCount: Int,
    val targetStepCount: Int,
    val completed: Boolean,
)
