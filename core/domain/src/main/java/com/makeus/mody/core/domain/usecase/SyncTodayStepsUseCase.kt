package com.makeus.mody.core.domain.usecase

import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRecordResult
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** 동기화 결과. 화면이 게이지를 즉시 갱신할 때 쓴다. */
data class StepSyncResult(
    /** 서버에 올린 날짜 수. 0 이면 올릴 게 없었다. */
    val uploadedDays: Int,
    /** 이번 동기화로 읽은 걸음 수 합(올린 날짜들의 합). */
    val readStepCount: Int,
    /** 마지막 upsert 응답의 그룹 누적값. 업로드가 없었거나 실패면 null. */
    val currentStepCount: Int?,
    val targetStepCount: Int?,
)

/**
 * 걸음 수를 서버에 반영한다. 앱 진입(포그라운드 복귀 포함)과 챌린지 탭 진입에서 호출.
 *
 * 서버는 `(groupChallengeId, memberId, recordedOn)` 단위로 덮어쓰고 그 합을 누적 걸음 수로
 * 쓴다. 그래서 여러 날짜를 몇 번 다시 올려도 값이 불어나지 않는다(idempotent) — 앱을 며칠
 * 안 켰어도 빠진 날짜를 그때 채워 넣을 수 있다.
 *
 * 카운트 시작점은 [StepChallengeStatus.fetchFromAt]. 챌린지를 바꾼 날은 이 값이 리셋
 * 시각(예: 14:40)이라, 그날은 0시가 아니라 14:40 부터 세야 리셋 전 걸음이 안 섞인다.
 *
 * 권한이 없으면 요청하지 않고 건너뛴다. 화면 없이도 도는 경로라 시스템 팝업이 뜨면 안 된다
 * (권한 요청은 챌린지 탭 담당).
 */
class SyncTodayStepsUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
    private val challengeRepository: ChallengeRepository,
    private val groupRepository: GroupRepository,
    private val sessionRepository: SessionRepository,
) {
    /**
     * @param groupId 이미 아는 그룹이 있으면 전달(재조회 생략). null 이면 직접 결정한다.
     * @return 동기화 결과. 미로그인·권한 없음·그룹 없음이면 null.
     */
    suspend operator fun invoke(groupId: Long? = null): StepSyncResult? {
        if (!runCatching { sessionRepository.isLoggedIn() }.getOrDefault(false)) return null
        if (healthRepository.availability() != HealthAvailability.AVAILABLE) return null
        if (!runCatching { healthRepository.hasStepPermission() }.getOrDefault(false)) return null

        val targetGroupId = groupId ?: resolveGroupId() ?: return null
        val challenge = runCatching { challengeRepository.getStepChallenge(targetGroupId) }
            .getOrNull()

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val from = countingStart(challenge, zone, now)

        var uploadedDays = 0
        var readTotal = 0
        var last: StepRecordResult? = null

        for ((date, range) in dayRanges(from, now, zone)) {
            val steps = runCatching { healthRepository.readStepCount(range.first, range.second) }
                .getOrDefault(0)
            // 0 은 올리지 않는다. 읽기 창(30일) 경계나 데이터 누락으로 나온 0 을 그대로 올리면
            // 덮어쓰기라서 서버에 쌓여 있던 그날 기록이 지워진다. 기록이 없는 날 = 0 이라 손해도 없다.
            if (steps <= 0) continue
            val result = runCatching {
                challengeRepository.upsertStepRecord(
                    groupId = targetGroupId,
                    recordedOn = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    stepCount = steps,
                )
            }.getOrNull()
            readTotal += steps
            if (result != null) {
                uploadedDays++
                last = result
            }
        }
        return StepSyncResult(
            uploadedDays = uploadedDays,
            readStepCount = readTotal,
            currentStepCount = last?.currentStepCount,
            targetStepCount = last?.targetStepCount,
        )
    }

    /**
     * 걸음 수를 세기 시작할 시각.
     *
     * 서버가 [StepChallengeStatus.fetchFromAt] 을 주면 그 값, 없으면 오늘 0시(기존 동작).
     * 어느 쪽이든 Health Connect 가 읽을 수 있는 창([HealthRepository.EARLIEST_READABLE_DAYS])
     * 안으로 자른다 — 그보다 오래된 구간은 에러가 아니라 0 이 돌아와서, 안 자르면 과거 기록을
     * 0 으로 덮어쓰게 된다.
     */
    private fun countingStart(
        challenge: StepChallengeStatus?,
        zone: ZoneId,
        now: Instant,
    ): Instant {
        val today = LocalDate.now(zone)
        val default = today.atStartOfDay(zone).toInstant()
        // 경계에 걸친 하루는 앞부분이 잘려 실제보다 적게 읽히므로, 온전히 읽을 수 있는
        // 날짜(오늘 - 29일)의 0시를 하한으로 둔다.
        val earliest = today.minusDays(HealthRepository.EARLIEST_READABLE_DAYS - 1L)
            .atStartOfDay(zone).toInstant()
        val from = challenge?.fetchFromAt ?: default
        return maxOf(from, earliest).coerceAtMost(now)
    }

    /**
     * [from] ~ [to] 를 날짜별 구간으로 쪼갠다.
     * 첫날은 [from] 부터(리셋 시각 반영), 마지막 날은 [to] 까지, 중간 날은 하루 전체.
     */
    private fun dayRanges(
        from: Instant,
        to: Instant,
        zone: ZoneId,
    ): List<Pair<LocalDate, Pair<Instant, Instant>>> {
        if (!to.isAfter(from)) return emptyList()
        val ranges = mutableListOf<Pair<LocalDate, Pair<Instant, Instant>>>()
        var date = from.atZone(zone).toLocalDate()
        val lastDate = to.atZone(zone).toLocalDate()
        while (!date.isAfter(lastDate)) {
            val dayStart = date.atStartOfDay(zone).toInstant()
            val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
            val start = maxOf(dayStart, from)
            val end = minOf(dayEnd, to)
            if (end.isAfter(start)) ranges += date to (start to end)
            date = date.plusDays(1)
        }
        return ranges
    }

    /** 마지막 선택 그룹(세션) > 첫 그룹. 챌린지 탭·피드의 그룹 결정 규칙과 동일. */
    private suspend fun resolveGroupId(): Long? {
        val groups = runCatching { groupRepository.getMyGroups() }.getOrNull() ?: return null
        val lastGroupId = runCatching { sessionRepository.getLastGroupId() }.getOrNull()
            ?.takeIf { id -> groups.any { it.groupId == id } }
        return lastGroupId ?: groups.firstOrNull()?.groupId
    }
}
