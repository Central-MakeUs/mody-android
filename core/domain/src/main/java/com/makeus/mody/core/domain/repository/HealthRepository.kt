package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.HealthAvailability
import java.time.Instant

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

    /**
     * [from] ~ [to] 구간의 걸음 수 합계. 권한 없거나 데이터 없으면 0.
     *
     * 챌린지가 리셋된 날처럼 하루의 일부만 세야 하는 경우와, 앱을 며칠 안 켠 뒤의
     * 날짜별 백필에 쓴다.
     *
     * 주의: Health Connect 는 권한 부여 시점 기준 과거 30일보다 오래된 구간에 대해
     * 에러가 아니라 0 을 돌려준다. 호출자가 범위를 제한해야 한다
     * ([EARLIEST_READABLE_DAYS]).
     */
    suspend fun readStepCount(from: Instant, to: Instant): Int

    companion object {
        /**
         * 안전하게 읽을 수 있는 최대 과거 일수. 이보다 오래된 날짜는 0 이 돌아오므로
         * 그대로 서버에 올리면 이미 쌓인 기록을 0 으로 덮어쓴다.
         */
        const val EARLIEST_READABLE_DAYS = 30
    }
}
