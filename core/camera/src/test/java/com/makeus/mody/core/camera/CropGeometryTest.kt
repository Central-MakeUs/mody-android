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

    /** 가로 사진 + 1:1 은 가이드가 사진 높이에 맞춰지고, 사진은 좌우로만 움직일 수 있다. */
    @Test
    fun `가로 사진에 정사각 가이드는 높이에 맞추고 사진은 좌우로만 움직인다`() {
        val l = layout(4000, 3000, SQUARE_FRAME_RATIO)
        assertEquals(vw, l.displayedWidth, EPS)
        assertEquals(810f, l.displayedHeight, EPS)
        assertEquals(810f, l.frameWidth, EPS)
        assertEquals(810f, l.frameHeight, EPS)
        val t = l.clamp(PhotoTransform(offsetX = 100_000f, offsetY = 100_000f))
        assertEquals((vw - 810f) / 2f, t.offsetX, EPS)
        assertEquals(0f, t.offsetY, EPS)
    }

    @Test
    fun `처음엔 사진 가운데를 잘라낸다`() {
        val l = layout(3000, 4000, 0.5f)
        val r = l.cropRegionAt(PhotoTransform())
        assertEquals(0f, r.x, EPS)
        assertEquals(1f, r.width, EPS)
        // 프레임 높이 540 / 1440 = 0.375
        assertEquals(0.375f, r.height, EPS)
        assertEquals((1f - 0.375f) / 2f, r.y, EPS)
    }

    /** 사진을 왼쪽 끝까지 밀면 가이드엔 사진 오른쪽 끝이 들어온다. */
    @Test
    fun `사진을 밀어도 가이드 안에 빈 곳이 생기지 않는다`() {
        val l = layout(4000, 3000, SQUARE_FRAME_RATIO)
        val left = l.cropRegionAt(l.clamp(PhotoTransform(offsetX = -100_000f)))
        assertEquals(1f, left.x + left.width, EPS)
        val right = l.cropRegionAt(l.clamp(PhotoTransform(offsetX = 100_000f)))
        assertEquals(0f, right.x, EPS)
    }

    @Test
    fun `배율은 1에서 3 사이로 묶인다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        assertEquals(1f, l.clamp(PhotoTransform(scale = 0.2f)).scale, 0f)
        assertEquals(MAX_ZOOM, l.clamp(PhotoTransform(scale = 10f)).scale, 0f)
    }

    /** 3배면 가이드에 들어오는 원본 폭이 1/3 이 된다. */
    @Test
    fun `3배 확대하면 크롭 영역이 3분의 1로 줄어든다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        val base = l.cropRegionAt(PhotoTransform())
        val zoomed = l.cropRegionAt(l.clamp(PhotoTransform(scale = 3f)))
        assertEquals(base.width / 3f, zoomed.width, EPS)
        assertEquals(base.height / 3f, zoomed.height, EPS)
        // 가운데 확대라 가운데를 유지한다
        assertEquals(0.5f, zoomed.x + zoomed.width / 2f, EPS)
        assertEquals(0.5f, zoomed.y + zoomed.height / 2f, EPS)
    }

    /** 3:4 사진은 배율 1 에서 좌우로 못 움직이지만, 확대하면 움직일 수 있다. */
    @Test
    fun `확대하면 막혀 있던 축으로도 움직인다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        assertEquals(0f, l.clamp(PhotoTransform(offsetX = 500f)).offsetX, EPS)
        assertTrue(l.clamp(PhotoTransform(scale = 2f, offsetX = 500f)).offsetX > 0f)
    }

    /** 확대 상태에서 끝까지 민 뒤 축소하면, 축소된 크기 범위로 다시 끌려와 빈 곳이 안 생긴다. */
    @Test
    fun `밀어둔 채 축소하면 범위 안으로 돌아온다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        val pushed = l.clamp(PhotoTransform(scale = 3f, offsetX = 100_000f))
        val shrunk = l.transformBy(pushed, l.centerX, l.centerY, 0f, 0f, zoom = 1f / 3f)
        assertEquals(1f, shrunk.scale, EPS)
        assertEquals(0f, shrunk.offsetX, EPS)
    }

    /** 손가락 아래 점이 확대 후에도 손가락 아래에 있어야 한다. */
    @Test
    fun `핀치는 손가락 위치를 기준으로 확대한다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        // 가이드 안쪽 한 점(가운데보다 오른쪽 아래)
        val px = l.centerX + 200f
        val py = l.centerY + 100f
        val t = l.transformBy(PhotoTransform(), px, py, 0f, 0f, zoom = 2f)
        assertEquals(2f, t.scale, 0f)
        // 확대 전 그 점의 사진 내 위치 = 확대 후 위치
        val before = (px - l.imageLeft) / l.displayedWidth
        val photoLeft = l.centerX + t.offsetX - l.displayedWidth * t.scale / 2f
        val after = (px - photoLeft) / (l.displayedWidth * t.scale)
        assertEquals(before, after, 0.001f)
    }

    @Test
    fun `드래그는 이동만 반영한다`() {
        val l = layout(3000, 4000, RECORD_FRAME_RATIO)
        val t = l.transformBy(PhotoTransform(), 0f, 0f, panX = 0f, panY = 30f, zoom = 1f)
        assertEquals(1f, t.scale, 0f)
        assertEquals(30f, t.offsetY, EPS)
    }

    /** 원본 px 로 되돌렸을 때 프레임 비율이 유지돼야 카드에서 찌그러지지 않는다. */
    @Test
    fun `크롭 영역은 원본 px 에서 프레임 비율을 지킨다`() {
        for ((w, h) in listOf(3000 to 4000, 4000 to 3000, 1080 to 2400, 6000 to 1500, 300 to 200)) {
            for (ratio in listOf(RECORD_FRAME_RATIO, SQUARE_FRAME_RATIO)) {
                val l = layout(w, h, ratio)
                for (scale in listOf(1f, 1.7f, MAX_ZOOM)) {
                    val r = l.cropRegionAt(l.clamp(PhotoTransform(scale = scale)))
                    assertEquals("${w}x$h ratio=$ratio scale=$scale", ratio, (r.height * h) / (r.width * w), 0.001f)
                }
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
                for (o in listOf(-100_000f, 0f, 100_000f)) for (scale in listOf(1f, MAX_ZOOM)) {
                    val r = l.cropRegionAt(l.clamp(PhotoTransform(scale, o, o)))
                    val label = "ratio=$ratio size=${w}x$h offset=$o scale=$scale"
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
        val r = l.cropRegionAt(PhotoTransform())
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
