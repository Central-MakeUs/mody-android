package com.makeus.mody.core.camera

import com.makeus.mody.core.model.CropRegion

/** 사진 최대 확대 배율. 처음 보이는 크기(aspect-fit) 기준. */
internal const val MAX_ZOOM = 3f

/**
 * 조정 화면의 사진·가이드 배치와 크롭 영역 계산.
 *
 * Composable 밖으로 빼둔 이유는 두 가지다. 하나는 화면 크기·사진 비율 조합마다
 * 손으로 확인할 수 없어서고(세로 사진·가로 사진·파노라마가 각각 다르게 동작한다),
 * 다른 하나는 [CropRegion] 이 원본 대비 정규화 비율이라 여기서 한 번 틀리면 업로드된
 * 사진이 엉뚱하게 잘려도 화면에선 안 보이기 때문이다.
 *
 * 좌표계: 편집 영역(3:4 박스) 기준 표시 px, 위에서 아래로 증가.
 *
 * - 사진은 처음에 편집 영역 안에 통째로 들어가게 맞춘다(aspect-fit). 남는 곳은 여백.
 * - 가이드(크롭 프레임)는 그 사진 안에 들어가는 가장 큰 크기로, 편집 영역 가운데에 고정된다.
 * - 사용자는 가이드 뒤의 사진을 확대([MAX_ZOOM] 까지)·이동한다([PhotoTransform]).
 *   가이드 안에 빈 곳이 보이지 않도록 축소·이동을 막는다 — 처음 크기(배율 1)가 가이드를
 *   빈틈없이 덮는 가장 작은 크기다.
 */
internal data class CropLayout(
    /** 배율 1 일 때 사진의 왼쪽 위(편집 영역 기준 px). */
    val imageLeft: Float,
    val imageTop: Float,
    /** 배율 1 일 때 사진 크기(px). */
    val displayedWidth: Float,
    val displayedHeight: Float,
    /** 가이드 크기(px). 배율 1 의 사진 안에 들어가는 프레임 비율의 최대 사각형. */
    val frameWidth: Float,
    val frameHeight: Float,
) {
    /** 배율 1 일 때 사진 가운데. 가이드 가운데이기도 하다. */
    val centerX: Float get() = imageLeft + displayedWidth / 2f
    val centerY: Float get() = imageTop + displayedHeight / 2f

    /** 가이드 왼쪽 위. 고정. */
    val guideLeft: Float get() = centerX - frameWidth / 2f
    val guideTop: Float get() = centerY - frameHeight / 2f
}

/**
 * 가이드 뒤 사진의 변환.
 *
 * @param scale 배율 1(aspect-fit) 대비 확대 배율. 1~[MAX_ZOOM].
 * @param offsetX 사진 가운데가 배율 1 위치에서 옮겨간 거리(px).
 */
internal data class PhotoTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

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
 * 배율을 1~[MAX_ZOOM] 으로, 이동을 사진이 가이드를 덮는 범위로 잘라낸다.
 *
 * 가이드가 사진 가운데에 있으므로 사진 가운데는 가이드 가운데에서 `(사진 크기 - 가이드 크기) / 2`
 * 까지만 움직일 수 있다. 배율 1 에서 가이드가 사진 폭(또는 높이)에 딱 맞는 축은 범위가 0 이다.
 */
internal fun CropLayout.clamp(transform: PhotoTransform): PhotoTransform {
    val scale = transform.scale.coerceIn(1f, MAX_ZOOM)
    val maxX = (displayedWidth * scale - frameWidth) / 2f
    val maxY = (displayedHeight * scale - frameHeight) / 2f
    return PhotoTransform(
        scale = scale,
        offsetX = transform.offsetX.clampOrCenter(-maxX, maxX),
        offsetY = transform.offsetY.clampOrCenter(-maxY, maxY),
    )
}

/**
 * 제스처 한 번(핀치·드래그)을 반영한다.
 *
 * 핀치는 두 손가락 가운데([centroidX], [centroidY]) 아래의 점이 손가락을 따라가도록 확대한다 —
 * 사진 가운데 기준으로 키우면 보던 곳이 손가락 밖으로 밀려난다.
 *
 * @param zoom 이번 이벤트의 배율 변화(1 = 그대로).
 * @param panX 이번 이벤트의 이동(px).
 */
internal fun CropLayout.transformBy(
    current: PhotoTransform,
    centroidX: Float,
    centroidY: Float,
    panX: Float,
    panY: Float,
    zoom: Float,
): PhotoTransform {
    val scale = (current.scale * zoom).coerceIn(1f, MAX_ZOOM)
    val k = scale / current.scale
    val photoX = centerX + current.offsetX
    val photoY = centerY + current.offsetY
    val newPhotoX = centroidX + (photoX - centroidX) * k + panX
    val newPhotoY = centroidY + (photoY - centroidY) * k + panY
    return clamp(PhotoTransform(scale, newPhotoX - centerX, newPhotoY - centerY))
}

/**
 * [min, max] 범위로 자른다.
 *
 * 범위가 0 이어야 하는 축이 부동소수 오차로 `[0.00003, -0.00003]` 처럼 비는 경우가 있다.
 * `coerceIn` 은 빈 범위에서 예외를 던지므로 그때는 범위 가운데에 고정한다.
 */
private fun Float.clampOrCenter(min: Float, max: Float): Float =
    if (max < min) (min + max) / 2f else coerceIn(min, max)

/**
 * 가이드에 들어온 사진 영역을 원본 대비 정규화 크롭 영역으로 환산한다.
 *
 * 사진을 aspect-fit 으로 그렸으므로 표시 좌표를 표시 크기로 나누면 그대로 원본 비율이다.
 * 부동소수 오차로 0~1 을 살짝 벗어날 수 있어 잘라낸다 — 그대로 넘기면 서버·뷰어 쪽에서
 * 어떻게 해석될지 모른다.
 */
internal fun CropLayout.cropRegionAt(transform: PhotoTransform): CropRegion {
    val photoWidth = displayedWidth * transform.scale
    val photoHeight = displayedHeight * transform.scale
    val photoLeft = centerX + transform.offsetX - photoWidth / 2f
    val photoTop = centerY + transform.offsetY - photoHeight / 2f
    val x = ((guideLeft - photoLeft) / photoWidth).coerceIn(0f, 1f)
    val y = ((guideTop - photoTop) / photoHeight).coerceIn(0f, 1f)
    val width = (frameWidth / photoWidth).coerceIn(0f, 1f - x)
    val height = (frameHeight / photoHeight).coerceIn(0f, 1f - y)
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
