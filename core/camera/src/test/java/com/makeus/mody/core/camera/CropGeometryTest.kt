package com.makeus.mody.core.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 크롭 기하 계산.
 *
 * 편집 영역은 3:4 (1080 x 1440). 실제 기기 값과 맞출 필요는 없다 — 비율 관계만 검증한다.
 */
class CropGeometryTest {

    private val vw = 1080f
    private val vh = 1440f

    private fun layout(w: Int, h: Int, ratio: Float) = cropLayout(vw, vh, w, h, ratio)

    @Test
    fun `3대4 사진은 편집 영역을 꽉 채운다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        assertEquals(0f, l.imageLeft, EPS)
        assertEquals(0f, l.imageTop, EPS)
        assertEquals(vw, l.displayedWidth, EPS)
        assertEquals(vh, l.displayedHeight, EPS)
        // 프레임은 사진 폭을 채운다
        assertEquals(vw, l.frameWidth, EPS)
    }

    /** 세로로 긴 사진(9:20)은 잘리지 않고 높이에 맞춰 통째로 들어가고, 좌우가 여백이다. */
    @Test
    fun `세로로 긴 사진은 잘리지 않고 좌우 여백이 생긴다`() {
        val l = layout(1080, 2400, RECORD_FRAME_RATIO)
        assertEquals(vh, l.displayedHeight, EPS)
        assertEquals(1440f * 1080f / 2400f, l.displayedWidth, EPS)
        assertTrue(l.imageLeft > 0f)
        // 프레임은 사진 폭으로 줄어든다(편집 영역 폭이 아니라)
        assertEquals(l.displayedWidth, l.frameWidth, EPS)
    }

    /** 가로 사진 + 1:1 은 프레임이 사진 높이에 맞춰지고, 좌우로 움직일 수 있다. */
    @Test
    fun `가로 사진에 정사각 프레임은 높이에 맞추고 좌우로 움직인다`() {
        val l = layout(4000, 3000, SQUARE_FRAME_RATIO)
        assertEquals(vw, l.displayedWidth, EPS)
        assertEquals(810f, l.displayedHeight, EPS)
        assertEquals(810f, l.frameWidth, EPS)
        assertEquals(810f, l.frameHeight, EPS)
        assertTrue(l.maxOffsetX > 0f)
        assertEquals(0f, l.maxOffsetY, EPS)
    }

    @Test
    fun `가운데 프레임은 사진 가운데를 잘라낸다`() {
        val l = layout(3000, 4000, 0.5f)
        val r = l.cropRegionAt(l.baseFrameLeft, l.baseFrameTop)
        assertEquals(0f, r.x, EPS)
        assertEquals(1f, r.width, EPS)
        // 프레임 높이 540 / 1440 = 0.375
        assertEquals(0.375f, r.height, EPS)
        assertEquals((1f - 0.375f) / 2f, r.y, EPS)
    }

    @Test
    fun `드래그는 사진 밖으로 나가지 않는다`() {
        val l = layout(4000, 3000, SQUARE_FRAME_RATIO)
        val (x, y) = l.clampOffset(100_000f, -100_000f)
        val r = l.cropRegionAt(l.baseFrameLeft + x, l.baseFrameTop + y)
        assertEquals(1f, r.x + r.width, EPS)
        assertEquals(0f, r.y, EPS)
        val (x2, _) = l.clampOffset(-100_000f, 0f)
        assertEquals(0f, l.cropRegionAt(l.baseFrameLeft + x2, l.baseFrameTop).x, EPS)
    }

    /** 원본 px 로 되돌렸을 때 프레임 비율이 유지돼야 카드에서 찌그러지지 않는다. */
    @Test
    fun `크롭 영역은 원본 px 에서 프레임 비율을 지킨다`() {
        for ((w, h) in listOf(3000 to 4000, 4000 to 3000, 1080 to 2400, 6000 to 1500, 300 to 200)) {
            for (ratio in listOf(RECORD_FRAME_RATIO, SQUARE_FRAME_RATIO)) {
                val l = layout(w, h, ratio)
                val r = l.cropRegionAt(l.baseFrameLeft, l.baseFrameTop)
                assertEquals("${w}x$h ratio=$ratio", ratio, (r.height * h) / (r.width * w), 0.001f)
            }
        }
    }

    /** 정규화 값이라 0~1 을 벗어나면 서버·뷰어가 어떻게 해석할지 모른다. */
    @Test
    fun `크롭 영역은 항상 0에서 1 사이에 머문다`() {
        val sizes = listOf(3000 to 4000, 4000 to 3000, 1000 to 1000, 6000 to 1500, 1080 to 2400)
        for (ratio in listOf(0.2f, RECORD_FRAME_RATIO, 1f, 1.5f)) {
            for ((w, h) in sizes) {
                val l = layout(w, h, ratio)
                for (o in listOf(-100_000f, 0f, 100_000f)) {
                    val (x, y) = l.clampOffset(o, o)
                    val r = l.cropRegionAt(l.baseFrameLeft + x, l.baseFrameTop + y)
                    val label = "ratio=$ratio size=${w}x$h offset=$o"
                    assertTrue("$label $r", r.x in 0f..1f && r.y in 0f..1f)
                    assertTrue("$label $r", r.x + r.width <= 1f + EPS && r.y + r.height <= 1f + EPS)
                }
            }
        }
    }

    @Test
    fun `회전한 3대4 사진도 정사각으로 자른다`() {
        val (w, h) = rotatedSize(3000, 4000, quarterTurns = 1)
        assertEquals(4000 to 3000, w to h)
        val l = layout(w, h, SQUARE_FRAME_RATIO)
        val r = l.cropRegionAt(l.baseFrameLeft, l.baseFrameTop)
        assertEquals(r.width * w, r.height * h, 1f)
    }

    @Test
    fun `오른쪽 회전은 네 번 누르면 제자리다`() {
        assertEquals(listOf(1, 2, 3, 0), turnsAfter(clockwise = true))
        assertEquals(3000 to 4000, rotatedSize(3000, 4000, 2))
    }

    /** 왼쪽 90° 는 시계 방향 270° 다. 파일에 굽는 쪽은 시계 방향 횟수만 안다. */
    @Test
    fun `왼쪽 회전은 시계 방향 3 2 1 0 순서다`() {
        assertEquals(listOf(3, 2, 1, 0), turnsAfter(clockwise = false))
    }

    @Test
    fun `왼쪽 오른쪽을 한 번씩 누르면 제자리다`() {
        assertEquals(0, rotateQuarterTurn(rotateQuarterTurn(0, clockwise = false), clockwise = true))
        assertEquals(2, rotateQuarterTurn(rotateQuarterTurn(2, clockwise = true), clockwise = false))
    }

    private fun turnsAfter(clockwise: Boolean): List<Int> {
        var turns = 0
        return List(4) { rotateQuarterTurn(turns, clockwise).also { turns = it } }
    }

    private companion object {
        const val EPS = 0.01f
    }
}
