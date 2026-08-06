package com.makeus.mody.core.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.repository.HealthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health Connect 기반 걸음 수 조회.
 *
 * 클라이언트는 SDK 사용 가능할 때만 만들 수 있으므로([HealthConnectClient.getOrCreate] 는
 * 미설치 기기에서 예외를 던진다) 매 호출마다 상태를 확인한 뒤 lazy 하게 생성한다.
 */
@Singleton
class HealthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthRepository {

    override fun availability(): HealthAvailability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthAvailability.UPDATE_REQUIRED
            else -> HealthAvailability.UNAVAILABLE
        }

    override val stepPermissions: Set<String> =
        setOf(HealthPermission.getReadPermission(StepsRecord::class))

    override suspend fun hasStepPermission(): Boolean {
        val client = clientOrNull() ?: return false
        return client.permissionController.getGrantedPermissions()
            .containsAll(stepPermissions)
    }

    override suspend fun readTodayStepCount(): Int {
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        return readStepCount(from = startOfDay, to = Instant.now())
    }

    override suspend fun readStepCount(from: Instant, to: Instant): Int {
        val client = clientOrNull() ?: return 0
        if (!hasStepPermission()) return 0
        // 자정 직후 등으로 범위가 역전되면 집계 요청이 실패하므로 방어.
        if (!to.isAfter(from)) return 0
        val result: AggregationResult = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(from, to),
            ),
        )
        return result[StepsRecord.COUNT_TOTAL]?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt() ?: 0
    }

    private fun clientOrNull(): HealthConnectClient? =
        if (availability() == HealthAvailability.AVAILABLE) {
            runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
        } else {
            null
        }
}
