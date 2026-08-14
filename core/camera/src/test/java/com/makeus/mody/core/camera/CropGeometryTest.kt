package com.makeus.mody.core.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 크롭 기하 계산.
 *
 * 화면 폭 1080, 프레임 비율 200/354 (주간 챌린지 인증 프레임) 를 기준으로 잡았다.
 * 실제 기기 값과 맞출 필요는 없다 — 비율 관계만 검증한다.
 */
class CropGeometryTest {

    private val screenW = 1080f
    private val screenH = 2400f

    /** 세로로 긴 사진(4:3 세로). 화면 폭에 맞추면 세로가 남아 드래그 여지가 생긴다. */
    private fun tallLayout(frameRatio: Float = 0.5f) = cropLayout(
        screenWidth = screenW,
        screenHeight = screenH,
        imageWidth = 3000,
        imageHeight = 4000,
        frameRatio = frameRatio,
    )

    @Test
    fun `프레임 높이는 화면 폭 곱하기 비율이다`() {
        assertEquals(540f, tallLayout(0.5f).frameHeight, EPS)
        assertEquals(1080f, tallLayout(1f).frameHeight, EPS)
    }

    @Test
    fun `사진은 화면 폭에 맞춰 확대되고 세로 중앙에 놓인다`() {
        val layout = tallLayout()
        // 1080 / 3000 = 0.36 → 표시 높이 4000 * 0.36 = 1440
        assertEquals(0.36f, layout.imageScale, EPS)
        assertEquals((2400f - 1440f) / 2f, layout.imageTop, EPS)
    }

    @Test
    fun `사진이 프레임보다 길면 드래그 여지가 있다`() {
        val layout = tallLayout()
        assertTrue(layout.hasRoom)
        assertTrue(layout.maxOffset > layout.minOffset)
    }

    /**
     * 사진이 프레임보다 짧으면 여지가 없다.
     *
     * 이 경우 maxFrameTop < minFrameTop 이라 `coerceIn` 을 그대로 부르면
     * IllegalArgumentException 이 난다. hasRoom 이 그걸 막는다.
     */
    @Test
    fun `사진이 프레임보다 짧으면 드래그 여지가 없고 중앙에 고정된다`() {
        val layout = cropLayout(
            screenWidth = screenW,
            screenHeight = screenH,
            imageWidth = 4000,
            imageHeight = 1000, // 표시 높이 270 < 프레임 540
            frameRatio = 0.5f,
        )
        assertFalse(layout.hasRoom)
        assertEquals(layout.baseFrameTop, layout.frameTopAt(9999f), EPS)
        assertEquals(layout.baseFrameTop, layout.frameTopAt(-9999f), EPS)
    }

    @Test
    fun `드래그가 경계를 넘으면 잘린다`() {
        val layout = tallLayout()
        assertEquals(layout.maxFrameTop, layout.frameTopAt(100_000f), EPS)
        assertEquals(layout.minFrameTop, layout.frameTopAt(-100_000f), EPS)
    }

    @Test
    fun `드래그를 안 하면 프레임은 화면 중앙이다`() {
        val layout = tallLayout()
        assertEquals(layout.baseFrameTop, layout.frameTopAt(0f), EPS)
    }

    @Test
    fun `크롭 영역은 항상 세로 슬라이스다`() {
        val region = tallLayout().let { it.cropRegionAt(it.frameTopAt(0f)) }
        assertEquals(0f, region.x, EPS)
        assertEquals(1f, region.width, EPS)
    }

    /**
     * 프레임을 중앙에 두면 크롭 영역도 사진 중앙이어야 한다.
     * 표시 높이 1440 중 프레임 540 → 540/1440 = 0.375, 위아래 여백이 같으므로 y = 0.3125.
     */
    @Test
    fun `중앙 프레임은 사진 중앙을 잘라낸다`() {
        val layout = tallLayout()
        val region = layout.cropRegionAt(layout.frameTopAt(0f))
        assertEquals(0.375f, region.height, EPS)
        assertEquals((1f - 0.375f) / 2f, region.y, EPS)
    }

    @Test
    fun `위로 끝까지 올리면 사진 맨 위부터 잘린다`() {
        val layout = tallLayout()
        val region = layout.cropRegionAt(layout.frameTopAt(-100_000f))
        assertEquals(0f, region.y, EPS)
        assertEquals(0.375f, region.height, EPS)
    }

    @Test
    fun `아래로 끝까지 내리면 사진 맨 아래에서 끝난다`() {
        val layout = tallLayout()
        val region = layout.cropRegionAt(layout.frameTopAt(100_000f))
        assertEquals(1f, region.y + region.height, EPS)
    }

    /** 정규화 값이라 0~1 을 벗어나면 서버·뷰어가 어떻게 해석할지 모른다. */
    @Test
    fun `크롭 영역은 항상 0에서 1 사이에 머문다`() {
        val ratios = listOf(0.2f, 0.5f, 200f / 354f, 1f, 1.5f)
        val sizes = listOf(3000 to 4000, 4000 to 3000, 1000 to 1000, 4000 to 1000)
        for (ratio in ratios) {
            for ((w, h) in sizes) {
                val layout = cropLayout(screenW, screenH, w, h, ratio)
                for (offset in listOf(-100_000f, 0f, 100_000f)) {
                    val region = layout.cropRegionAt(layout.frameTopAt(offset))
                    val label = "ratio=$ratio size=${w}x$h offset=$offset"
                    assertTrue("$label y=${region.y}", region.y in 0f..1f)
                    assertTrue("$label height=${region.height}", region.height in 0f..1f)
                    assertTrue("$label y+h=${region.y + region.height}", region.y + region.height <= 1f + EPS)
                }
            }
        }
    }

    private companion object {
        const val EPS = 0.0001f
    }
}
