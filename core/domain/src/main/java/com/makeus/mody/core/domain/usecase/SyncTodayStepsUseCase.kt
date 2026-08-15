package com.makeus.mody.core.domain.usecase

import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRecordResult
import com.makeus.mody.core.domain.model.StepSyncCheckpoint
import com.makeus.mody.core.domain.error.ErrorReporter
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 체크포인트를 세우지 않고 매번 다시 읽는 최근 일수.
 *
 * 워치처럼 나중에 동기화되는 기기가 있어 어제 걸음 수가 오늘 들어오기도 한다. 오늘과 어제는
 * 항상 다시 읽어 늦게 온 데이터를 반영한다.
 */
private const val RESYNC_DAYS = 2L

/** 동기화 결과. 화면이 게이지를 즉시 갱신할 때 쓴다. */
data class StepSyncResult(
    /** 서버에 올린 날짜 수. 0 이면 올릴 게 없었다. */
    val uploadedDays: Int,
    /**
     * 이번 동기화로 올린 날짜들의 걸음 수 합.
     *
     * **누적 걸음 수가 아니다.** 최근 며칠치 증분이라 화면의 "달성 걸음수" 자리에 쓰면
     * 안 된다. 그 값은 [currentStepCount] 뿐이고, 없으면 없는 대로 둬야 한다.
     */
    val readStepCount: Int,
    /**
     * 마지막 upsert 응답의 그룹 누적값(서버 재계산). 올린 날이 하나도 없으면 null.
     *
     * null 을 0 으로 대신 채우면 안 된다 — "모른다"와 "0보"는 다르다.
     */
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
    private val errorReporter: ErrorReporter,
) {
    /**
     * @param groupId 이미 아는 그룹이 있으면 전달(재조회 생략). null 이면 직접 결정한다.
     * @param challenge 이미 받아둔 현황이 있으면 전달(재조회 생략). null 이면 직접 조회한다.
     * @return 동기화 결과. 미로그인·권한 없음·그룹 없음이면 null.
     */
    suspend operator fun invoke(
        groupId: Long? = null,
        challenge: StepChallengeStatus? = null,
    ): StepSyncResult? {
        if (!runCatching { sessionRepository.isLoggedIn() }.getOrDefault(false)) return null
        if (healthRepository.availability() != HealthAvailability.AVAILABLE) return null
        if (!runCatching { healthRepository.hasStepPermission() }.getOrDefault(false)) return null

        val targetGroupId = groupId ?: resolveGroupId() ?: return null
        // 호출부가 방금 받아둔 값이 있으면 그걸 쓴다 — 없을 때만 부른다.
        // 여기서 쓰는 건 카운트 시작 시각뿐이라, 조회 직후 값이면 다시 받아도 같다.
        val targetChallenge = challenge
            ?: runCatching { challengeRepository.getStepChallenge(targetGroupId) }.getOrNull()

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        // 챌린지가 정한 카운트 기준(클램프 전). 체크포인트 유효성 판정에 이 값을 쓴다 —
        // 아래 countingStart 는 읽기 창 하한이 매일 밀려서 날짜가 바뀔 때마다 달라진다.
        val anchor = countingAnchor(targetChallenge, zone)
        val countingStart = clampToReadableWindow(anchor, zone, now)
        val from = resumePoint(countingStart, anchor, targetGroupId, zone)

        var uploadedDays = 0
        var readTotal = 0
        var last: StepRecordResult? = null
        // 앞에서부터 연속으로 끝난 마지막 날짜. 중간에 하나라도 실패하면 여기서 멈춘다 —
        // 실패한 날을 넘겨 체크포인트를 세우면 그날 걸음 수는 영영 안 올라간다.
        var contiguousDone: LocalDate? = null
        var blocked = false

        for ((date, range) in dayRanges(from, now, zone)) {
            // 읽기 실패는 걸음 수가 안 오르는 것으로만 드러난다(화면엔 아무 표시도 없다).
            // 서버 업로드 실패는 네트워크 계층이 이미 보고하므로 여기선 읽기만 남긴다.
            val steps = runCatching { healthRepository.readStepCount(range.first, range.second) }
                .onFailure { e ->
                    errorReporter.report(e, mapOf("source" to "health_connect_read_steps"))
                }
                .getOrNull()
            // 읽기 실패(null)와 진짜 0 을 구분한다. 둘 다 올리지는 않지만, 실패한 날은
            // "끝났다"고 볼 수 없어 체크포인트를 여기서 멈춘다.
            if (steps == null) {
                blocked = true
                continue
            }
            // 0 은 올리지 않는다. 읽기 창(30일) 경계나 데이터 누락으로 나온 0 을 그대로 올리면
            // 덮어쓰기라서 서버에 쌓여 있던 그날 기록이 지워진다. 기록이 없는 날 = 0 이라 손해도 없다.
            if (steps <= 0) {
                if (!blocked) contiguousDone = date
                continue
            }
            val result = runCatching {
                challengeRepository.upsertStepRecord(
                    groupId = targetGroupId,
                    recordedOn = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    stepCount = steps,
                )
            }.getOrNull()
            // 서버가 받아준 날만 센다. 실패한 날까지 더하면 게이지가 서버에 없는 걸음 수를 보여준다.
            if (result != null) {
                readTotal += steps
                uploadedDays++
                last = result
                if (!blocked) contiguousDone = date
            } else {
                blocked = true
            }
        }
        saveCheckpoint(contiguousDone, anchor, targetGroupId, zone)
        return StepSyncResult(
            uploadedDays = uploadedDays,
            readStepCount = readTotal,
            currentStepCount = last?.currentStepCount,
            targetStepCount = last?.targetStepCount,
        )
    }

    /**
     * 실제로 읽기 시작할 시각. 체크포인트가 있으면 그 뒤부터.
     *
     * 체크포인트를 버리는 조건:
     * - 그룹이 다르다 — 다른 그룹의 진행도다. (체크포인트는 하나뿐이라 그룹을 오가면 매번
     *   처음부터 센다. 그룹이 여럿인 사용자가 드물어 단순한 쪽을 택했다.)
     * - 카운트 기준이 다르다 — 챌린지가 바뀌어 세는 시작점이 달라졌다.
     */
    private suspend fun resumePoint(
        countingStart: Instant,
        anchor: Instant,
        groupId: Long,
        zone: ZoneId,
    ): Instant {
        val checkpoint = runCatching { sessionRepository.getStepSyncCheckpoint() }.getOrNull()
            ?: return countingStart
        if (checkpoint.groupId != groupId) return countingStart
        if (checkpoint.anchorEpochMs != anchor.toEpochMilli()) return countingStart
        val syncedThrough = runCatching { LocalDate.parse(checkpoint.syncedThrough) }.getOrNull()
            ?: return countingStart
        // 읽기 창 하한(countingStart)보다 앞으로는 못 간다.
        return maxOf(countingStart, syncedThrough.plusDays(1).atStartOfDay(zone).toInstant())
    }

    /**
     * 연속으로 끝난 마지막 날짜를 기록한다.
     *
     * 최근 [RESYNC_DAYS] 일은 기록하지 않고 잘라낸다 — 워치처럼 나중에 동기화되는 기기가 있어
     * 어제 걸음 수가 오늘 들어오기도 한다. 그 창을 남겨두면 늦게 온 데이터도 다음 동기화에
     * 반영된다. 안정 상태에서 읽기/업로드는 이 창 크기로 수렴한다.
     */
    private suspend fun saveCheckpoint(
        contiguousDone: LocalDate?,
        anchor: Instant,
        groupId: Long,
        zone: ZoneId,
    ) {
        if (contiguousDone == null) return
        val cutoff = LocalDate.now(zone).minusDays(RESYNC_DAYS)
        // 오늘까지 다 끝났어도 재동기화 창만큼은 뒤로 물려 기록한다.
        val through = if (contiguousDone.isAfter(cutoff)) cutoff else contiguousDone
        // 카운트 시작일보다 앞선 날짜는 기록해봐야 재개점을 못 당긴다.
        if (through.isBefore(anchor.atZone(zone).toLocalDate())) return
        runCatching {
            sessionRepository.saveStepSyncCheckpoint(
                StepSyncCheckpoint(
                    groupId = groupId,
                    anchorEpochMs = anchor.toEpochMilli(),
                    syncedThrough = through.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ),
            )
        }
    }

    /**
     * 챌린지가 정한 카운트 기준 시각.
     *
     * 서버가 [StepChallengeStatus.fetchFromAt] 을 주면 그 값, 없으면 오늘 0시(기존 동작).
     * 읽기 창으로 자르기 전 값이라 날짜가 바뀌어도 안 변한다 — 체크포인트 유효성 판정에 쓴다.
     */
    private fun countingAnchor(challenge: StepChallengeStatus?, zone: ZoneId): Instant =
        challenge?.fetchFromAt ?: LocalDate.now(zone).atStartOfDay(zone).toInstant()

    /**
     * 실제로 읽을 수 있는 시작 시각.
     *
     * Health Connect 가 읽을 수 있는 창([HealthRepository.EARLIEST_READABLE_DAYS]) 안으로
     * 자른다 — 그보다 오래된 구간은 에러가 아니라 0 이 돌아와서, 안 자르면 과거 기록을
     * 0 으로 덮어쓰게 된다.
     */
    private fun clampToReadableWindow(anchor: Instant, zone: ZoneId, now: Instant): Instant {
        // 경계에 걸친 하루는 앞부분이 잘려 실제보다 적게 읽히므로, 온전히 읽을 수 있는
        // 날짜(오늘 - 29일)의 0시를 하한으로 둔다.
        val earliest = LocalDate.now(zone)
            .minusDays(HealthRepository.EARLIEST_READABLE_DAYS - 1L)
            .atStartOfDay(zone).toInstant()
        return maxOf(anchor, earliest).coerceAtMost(now)
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
