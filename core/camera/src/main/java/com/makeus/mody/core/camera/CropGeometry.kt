package com.makeus.mody.core.camera

import com.makeus.mody.core.model.CropRegion

/**
 * 조정 화면의 사진·프레임 배치와 크롭 영역 계산.
 *
 * Composable 밖으로 빼둔 이유는 두 가지다. 하나는 화면 크기·사진 비율 조합마다
 * 손으로 확인할 수 없어서고(세로 사진·가로 사진·파노라마가 각각 다르게 동작한다),
 * 다른 하나는 [CropRegion] 이 원본 대비 정규화 비율이라 여기서 한 번 틀리면 업로드된
 * 사진이 엉뚱하게 잘려도 화면에선 안 보이기 때문이다.
 *
 * 좌표계: 편집 영역(3:4 박스) 기준 표시 px, 위에서 아래로 증가.
 *
 * - 사진은 편집 영역 안에 통째로 들어가게 맞춘다(aspect-fit). 잘리는 곳 없이 남는 곳은 여백.
 * - 프레임은 사진 안에 들어가는 가장 큰 크기다. 사진 밖으로는 나가지 않으므로 프레임 안에
 *   빈 곳이 생기지 않고, 보이는 영역이 그대로 업로드 결과가 된다.
 * - 프레임은 상하좌우로 움직인다(iOS ROIOverlayView 와 같은 방식).
 */
internal data class CropLayout(
    /** 화면에 그려진 사진의 왼쪽 위(편집 영역 기준 px). */
    val imageLeft: Float,
    val imageTop: Float,
    /** 화면에 그려진 사진 크기(px). */
    val displayedWidth: Float,
    val displayedHeight: Float,
    /** 프레임 크기(px). 사진 안에 들어가는 프레임 비율의 최대 사각형. */
    val frameWidth: Float,
    val frameHeight: Float,
) {
    /** 드래그 전 프레임 왼쪽 위 — 사진 가운데. */
    val baseFrameLeft: Float get() = imageLeft + (displayedWidth - frameWidth) / 2f
    val baseFrameTop: Float get() = imageTop + (displayedHeight - frameHeight) / 2f

    /** 드래그 오프셋 범위. 프레임이 사진 밖으로 나가지 않는 만큼. */
    val minOffsetX: Float get() = imageLeft - baseFrameLeft
    val maxOffsetX: Float get() = imageLeft + displayedWidth - frameWidth - baseFrameLeft
    val minOffsetY: Float get() = imageTop - baseFrameTop
    val maxOffsetY: Float get() = imageTop + displayedHeight - frameHeight - baseFrameTop
}

/**
 * 편집 영역·사진·프레임 비율로 배치를 계산한다.
 *
 * @param viewportWidth 편집 영역 폭(px).
 * @param viewportHeight 편집 영역 높이(px).
 * @param imageWidth 회전을 반영한 사진 폭(px). [rotatedSize] 참고.
 * @param frameRatio 프레임 높이/너비.
 */
internal fun cropLayout(
    viewportWidth: Float,
    viewportHeight: Float,
    imageWidth: Int,
    imageHeight: Int,
    frameRatio: Float,
): CropLayout {
    val scale = minOf(viewportWidth / imageWidth, viewportHeight / imageHeight)
    val displayedWidth = imageWidth * scale
    val displayedHeight = imageHeight * scale
    // 폭을 사진 폭에 맞추되, 그러면 사진보다 높아지는 경우(가로 사진 + 1:1 등)는 높이에 맞춘다.
    val frameWidth = minOf(displayedWidth, displayedHeight / frameRatio)
    return CropLayout(
        imageLeft = (viewportWidth - displayedWidth) / 2f,
        imageTop = (viewportHeight - displayedHeight) / 2f,
        displayedWidth = displayedWidth,
        displayedHeight = displayedHeight,
        frameWidth = frameWidth,
        frameHeight = frameWidth * frameRatio,
    )
}

/**
 * 드래그 오프셋을 사진 안으로 잘라낸다.
 *
 * 프레임이 사진 폭(또는 높이)에 딱 맞는 축은 범위가 0 이어야 하지만, 부동소수 오차로
 * `[0, -0.00003]` 처럼 비는 경우가 있다. `coerceIn` 은 빈 범위에서 예외를 던지므로 그때는
 * 범위 가운데에 고정한다.
 */
internal fun CropLayout.clampOffset(offsetX: Float, offsetY: Float): Pair<Float, Float> =
    offsetX.clampOrCenter(minOffsetX, maxOffsetX) to offsetY.clampOrCenter(minOffsetY, maxOffsetY)

private fun Float.clampOrCenter(min: Float, max: Float): Float =
    if (max < min) (min + max) / 2f else coerceIn(min, max)

/**
 * 프레임 위치(표시 좌표)를 원본 대비 정규화 크롭 영역으로 환산한다.
 *
 * 사진을 aspect-fit 으로 그렸으므로 표시 좌표를 표시 크기로 나누면 그대로 원본 비율이다.
 * 부동소수 오차로 0~1 을 살짝 벗어날 수 있어 잘라낸다 — 그대로 넘기면 서버·뷰어 쪽에서
 * 어떻게 해석될지 모른다.
 */
internal fun CropLayout.cropRegionAt(frameLeft: Float, frameTop: Float): CropRegion {
    val x = ((frameLeft - imageLeft) / displayedWidth).coerceIn(0f, 1f)
    val y = ((frameTop - imageTop) / displayedHeight).coerceIn(0f, 1f)
    val width = (frameWidth / displayedWidth).coerceIn(0f, 1f - x)
    val height = (frameHeight / displayedHeight).coerceIn(0f, 1f - y)
    return CropRegion(x = x, y = y, width = width, height = height)
}

/** 시계 방향 90° × [quarterTurns] 회전 후의 (폭, 높이). 홀수 번이면 두 변이 바뀐다. */
internal fun rotatedSize(width: Int, height: Int, quarterTurns: Int): Pair<Int, Int> =
    if (quarterTurns % 2 == 0) width to height else height to width

/**
 * 회전 버튼 한 번. 시계 방향 기준 횟수(0~3)로 저장한다.
 * 오른쪽: 0 → 1 → 2 → 3 → 0, 왼쪽: 0 → 3 → 2 → 1 → 0.
 */
internal fun rotateQuarterTurn(quarterTurns: Int, clockwise: Boolean): Int =
    (quarterTurns + if (clockwise) 1 else 3) % 4
