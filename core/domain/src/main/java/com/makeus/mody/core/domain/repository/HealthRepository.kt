package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.HealthAvailability

/**
 * 기기 건강 데이터(걸음 수) 접근.
 * 구현체는 Health Connect 를 쓰지만, 도메인은 권한 문자열만 알고 SDK 타입은 모른다.
 */
interface HealthRepository {
    /** 이 기기에서 건강 데이터를 쓸 수 있는지. */
    fun availability(): HealthAvailability

    /**
     * 권한 요청 계약에 넘길 권한 문자열 집합.
     * (UI 레이어의 ActivityResultContract 가 그대로 사용)
     */
    val stepPermissions: Set<String>

    /** 걸음 수 읽기 권한이 이미 허용돼 있는지. */
    suspend fun hasStepPermission(): Boolean

    /** 오늘 0시부터 지금까지 누적 걸음 수. 권한 없거나 데이터 없으면 0. */
    suspend fun readTodayStepCount(): Int
}
