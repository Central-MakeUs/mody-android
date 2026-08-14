package com.makeus.mody.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 걸음 수 동기화 기간 계산.
 *
 * 시간대는 KST 고정. UTC 로 두면 자정 경계 계산이 우연히 맞아떨어져서, 실제로 쓰는
 * 오프셋(+9)에서만 나는 어긋남을 놓친다.
 */
class StepSyncWindowTest {

    private val zone = ZoneId.of("Asia/Seoul")

    private fun at(date: String, time: String = "00:00"): Instant =
        LocalDateTime.parse("${date}T$time").atZone(zone).toInstant()

    // --- dayRanges ---

    @Test
    fun `하루 안에서 끝나면 구간은 하나다`() {
        val ranges = dayRanges(at("2026-08-14", "09:00"), at("2026-08-14", "18:00"), zone)

        assertEquals(1, ranges.size)
        assertEquals(LocalDate.parse("2026-08-14"), ranges[0].date)
        assertEquals(at("2026-08-14", "09:00"), ranges[0].start)
        assertEquals(at("2026-08-14", "18:00"), ranges[0].end)
    }

    /**
     * 첫날은 시작 시각부터, 마지막 날은 끝 시각까지, 가운데는 하루 전체.
     * 서버가 날짜 단위로 덮어쓰므로 가운데 날이 하루보다 짧으면 그날 값이 깎여 확정된다.
     */
    @Test
    fun `여러 날에 걸치면 첫날과 마지막 날만 잘린다`() {
        val ranges = dayRanges(at("2026-08-12", "20:00"), at("2026-08-14", "07:00"), zone)

        assertEquals(3, ranges.size)

        assertEquals(at("2026-08-12", "20:00"), ranges[0].start)
        assertEquals(at("2026-08-13"), ranges[0].end)

        assertEquals(at("2026-08-13"), ranges[1].start)
        assertEquals(at("2026-08-14"), ranges[1].end)

        assertEquals(at("2026-08-14"), ranges[2].start)
        assertEquals(at("2026-08-14", "07:00"), ranges[2].end)
    }

    @Test
    fun `가운데 날은 정확히 24시간이다`() {
        val ranges = dayRanges(at("2026-08-12", "20:00"), at("2026-08-15", "07:00"), zone)

        for (range in ranges.drop(1).dropLast(1)) {
            val hours = java.time.Duration.between(range.start, range.end).toHours()
            assertEquals("${range.date} 가 24시간이 아니다", 24L, hours)
        }
    }

    @Test
    fun `날짜가 연속하고 겹치지 않는다`() {
        val ranges = dayRanges(at("2026-08-10", "13:00"), at("2026-08-14", "07:00"), zone)

        for (i in 1 until ranges.size) {
            assertEquals(ranges[i - 1].date.plusDays(1), ranges[i].date)
            assertEquals(ranges[i - 1].end, ranges[i].start)
        }
    }

    @Test
    fun `끝이 시작보다 앞이거나 같으면 빈 목록이다`() {
        assertTrue(dayRanges(at("2026-08-14", "10:00"), at("2026-08-14", "10:00"), zone).isEmpty())
        assertTrue(dayRanges(at("2026-08-14", "10:00"), at("2026-08-13", "10:00"), zone).isEmpty())
    }

    /** from 이 정확히 자정이면 그날이 통째로, 앞날은 길이 0 이라 안 잡혀야 한다. */
    @Test
    fun `자정에서 시작하면 길이 0인 구간이 생기지 않는다`() {
        val ranges = dayRanges(at("2026-08-13"), at("2026-08-14", "07:00"), zone)

        assertEquals(2, ranges.size)
        assertEquals(LocalDate.parse("2026-08-13"), ranges[0].date)
        assertTrue(ranges.all { it.end.isAfter(it.start) })
    }

    /** to 가 정확히 자정이면 그 날짜는 길이 0 이라 빠진다. */
    @Test
    fun `자정에서 끝나면 그날은 포함되지 않는다`() {
        val ranges = dayRanges(at("2026-08-13", "10:00"), at("2026-08-14"), zone)

        assertEquals(1, ranges.size)
        assertEquals(LocalDate.parse("2026-08-13"), ranges[0].date)
    }

    // --- clampToReadableWindow ---

    /**
     * 읽기 창(30일)보다 오래된 anchor 는 잘린다. 안 자르면 Health Connect 가 에러가 아니라
     * 0 을 돌려줘서, 서버에 쌓여 있던 과거 기록을 0 으로 덮어쓴다.
     */
    @Test
    fun `읽기 창보다 오래된 시작점은 잘린다`() {
        val now = at("2026-08-14", "15:00")
        val anchor = at("2026-01-01", "00:00")

        // 온전히 읽을 수 있는 날 = 오늘 - 29일 = 07-16 의 0시
        assertEquals(at("2026-07-16"), clampToReadableWindow(anchor, zone, now))
    }

    @Test
    fun `읽기 창 안의 시작점은 그대로 둔다`() {
        val now = at("2026-08-14", "15:00")
        val anchor = at("2026-08-10", "06:30")

        assertEquals(anchor, clampToReadableWindow(anchor, zone, now))
    }

    /** 챌린지 시작이 미래면(서버 값 이상 등) 지금을 넘지 않아야 빈 구간이 된다. */
    @Test
    fun `시작점이 미래여도 현재를 넘지 않는다`() {
        val now = at("2026-08-14", "15:00")
        val anchor = at("2026-09-01", "00:00")

        assertEquals(now, clampToReadableWindow(anchor, zone, now))
    }

    /**
     * 하한은 `now` 에서만 끌어낸다.
     *
     * 예전에는 `LocalDate.now(zone)` 을 따로 불러 시각 소스가 둘이었다. 자정 직전에
     * 호출하면 두 시각이 다른 날짜를 가리켜 하한이 하루 어긋날 수 있었다.
     */
    @Test
    fun `하한은 인자로 받은 현재 시각만 따른다`() {
        val justBeforeMidnight = at("2026-08-14", "23:59")
        val justAfterMidnight = at("2026-08-15", "00:01")
        val anchor = at("2026-01-01")

        assertEquals(at("2026-07-16"), clampToReadableWindow(anchor, zone, justBeforeMidnight))
        assertEquals(at("2026-07-17"), clampToReadableWindow(anchor, zone, justAfterMidnight))
    }

    /** 잘린 시작점으로 구간을 쪼개면 30일을 넘지 않는다. */
    @Test
    fun `잘린 구간은 30일을 넘지 않는다`() {
        val now = at("2026-08-14", "15:00")
        val from = clampToReadableWindow(at("2026-01-01"), zone, now)

        assertTrue(dayRanges(from, now, zone).size <= 30)
    }
}
