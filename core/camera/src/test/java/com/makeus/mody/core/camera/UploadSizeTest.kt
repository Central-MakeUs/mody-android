package com.makeus.mody.core.camera

import org.junit.Assert.assertEquals
import org.junit.Test

/** 업로드본 크기 계산. 목표 장변은 2048. */
class UploadSizeTest {

    private val target = 2048

    /** 12MP(4000x3000)는 1/2 로 줄이면 2000 이라 목표보다 작아진다 → 건너뛰지 않고 보간만 한다. */
    @Test
    fun `12MP 는 샘플링하지 않고 보간으로 2048 에 맞춘다`() {
        assertEquals(1, decodeSampleSize(4000, target))
        assertEquals(2048, (4000 * downscaleFactor(4000, target)).toInt())
    }

    /** 50MP(8160x6120)는 1/2 로 4080 까지만 줄이고 나머지는 보간. 1/4(2040)로 가면 목표 미달. */
    @Test
    fun `50MP 는 목표 아래로 내려가기 전까지만 샘플링한다`() {
        assertEquals(2, decodeSampleSize(8160, target))
        assertEquals(2048, (4080 * downscaleFactor(4080, target)).toInt())
    }

    /** 이전 방식(장변 2048 이하가 될 때까지 샘플링)에선 6000 이 1500 으로 줄었다. */
    @Test
    fun `파노라마도 장변 2048 을 유지한다`() {
        assertEquals(2, decodeSampleSize(6000, target))
        assertEquals(2048, (3000 * downscaleFactor(3000, target)).toInt())
    }

    @Test
    fun `목표보다 작은 사진은 키우지 않는다`() {
        assertEquals(1, decodeSampleSize(1200, target))
        assertEquals(1f, downscaleFactor(1200, target), 0f)
    }

    @Test
    fun `정확히 목표 크기면 그대로 둔다`() {
        assertEquals(1, decodeSampleSize(2048, target))
        assertEquals(1f, downscaleFactor(2048, target), 0f)
    }

    /** 정확히 2배면 1/2 샘플링만으로 목표에 딱 맞는다. */
    @Test
    fun `정확히 2배면 샘플링만으로 끝난다`() {
        assertEquals(2, decodeSampleSize(4096, target))
        assertEquals(1f, downscaleFactor(2048, target), 0f)
    }
}
