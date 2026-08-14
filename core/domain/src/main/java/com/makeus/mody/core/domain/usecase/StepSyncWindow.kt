package com.makeus.mody.core.domain.usecase

import com.makeus.mody.core.domain.repository.HealthRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 걸음 수 동기화가 읽어올 기간 계산.
 *
 * [SyncTodayStepsUseCase] 안에 두면 Health Connect·서버·세션이 전부 엮여 있어
 * 확인할 방법이 없다. 여기 있는 건 시각 계산뿐이라 테스트로 고정할 수 있다.
 *
 * 여기서 틀리면 증상이 "걸음 수가 조금 이상하다" 로만 나타난다 — 하루가 통째로
 * 빠지거나, 경계에 걸친 날이 0 으로 덮여도 화면엔 에러가 없다.
 */

/** 하루치 읽기 구간. [start] 이상 [end] 미만. */
internal data class DayRange(
    val date: LocalDate,
    val start: Instant,
    val end: Instant,
)

/**
 * 실제로 읽을 수 있는 시작 시각.
 *
 * Health Connect 가 읽을 수 있는 창([HealthRepository.EARLIEST_READABLE_DAYS]) 안으로
 * 자른다 — 그보다 오래된 구간은 에러가 아니라 0 이 돌아와서, 안 자르면 과거 기록을
 * 0 으로 덮어쓰게 된다.
 *
 * 경계에 걸친 하루는 앞부분이 잘려 실제보다 적게 읽히므로, 온전히 읽을 수 있는
 * 날짜(오늘 - 29일)의 0시를 하한으로 둔다.
 *
 * 오늘 날짜는 [now] 에서 끌어낸다. 예전에는 `LocalDate.now(zone)` 을 따로 불러서
 * 인자로 받은 [now] 와 다른 시각을 볼 수 있었다 — 실제로는 호출 간격이 짧아 문제가
 * 안 났지만, 시각 소스가 둘이면 테스트가 불가능하고 자정 경계에서 갈릴 수 있다.
 */
internal fun clampToReadableWindow(anchor: Instant, zone: ZoneId, now: Instant): Instant {
    val earliest = now.atZone(zone).toLocalDate()
        .minusDays(HealthRepository.EARLIEST_READABLE_DAYS - 1L)
        .atStartOfDay(zone)
        .toInstant()
    return maxOf(anchor, earliest).coerceAtMost(now)
}

/**
 * [from] ~ [to] 를 날짜별 구간으로 쪼갠다.
 * 첫날은 [from] 부터(리셋 시각 반영), 마지막 날은 [to] 까지, 중간 날은 하루 전체.
 *
 * 서버가 날짜 단위로 덮어쓰기 때문에 하루를 통째로 읽어야 한다 — 구간이 하루를 넘거나
 * 모자라면 그날 걸음 수가 틀린 값으로 확정된다.
 */
internal fun dayRanges(from: Instant, to: Instant, zone: ZoneId): List<DayRange> {
    if (!to.isAfter(from)) return emptyList()

    val ranges = mutableListOf<DayRange>()
    var date = from.atZone(zone).toLocalDate()
    val lastDate = to.atZone(zone).toLocalDate()
    while (!date.isAfter(lastDate)) {
        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        val start = maxOf(dayStart, from)
        val end = minOf(dayEnd, to)
        // 길이 0 인 구간은 버린다 — from 이 정확히 자정이면 전날이 빈 구간으로 잡힌다.
        if (end.isAfter(start)) ranges += DayRange(date, start, end)
        date = date.plusDays(1)
    }
    return ranges
}
