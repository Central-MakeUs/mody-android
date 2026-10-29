package com.makeus.mody.core.camera

import com.makeus.mody.core.model.CropRegion

/**
 * 조정 화면의 프레임·사진 배치와 크롭 영역 계산.
 *
 * Composable 밖으로 빼둔 이유는 두 가지다. 하나는 화면 크기·사진 비율 조합마다
 * 손으로 확인할 수 없어서고(세로 사진·가로 사진·프레임보다 짧은 사진이 각각 다르게
 * 동작한다), 다른 하나는 [CropRegion] 이 원본 대비 정규화 비율이라 여기서 한 번
 * 틀리면 업로드된 사진이 엉뚱하게 잘려도 화면에선 안 보이기 때문이다.
 *
 * 좌표계: 표시 좌표(px), 위에서 아래로 증가. 프레임은 항상 화면 폭을 채우고 위아래로만
 * 움직인다. 사진은 프레임을 완전히 덮는 크기 이상으로 놓여, 프레임 안에 빈 곳이 생기지 않는다.
 */
internal data class CropLayout(
    /** 프레임 높이(표시 px). */
    val frameHeight: Float,
    /** 화면에 그려진 사진의 왼쪽 변(표시 px). 화면보다 넓으면 음수다. */
    val imageLeft: Float,
    /** 화면에 그려진 사진의 윗변(표시 px). 사진은 중앙 고정이라 음수가 될 수 있다. */
    val imageTop: Float,
    /** 원본 px → 표시 px 배율. */
    val imageScale: Float,
    /** 원본 사진 크기(px). 정규화에 쓴다. */
    val imageWidthPx: Int,
    val imageHeightPx: Int,
    /** 화면 폭(표시 px) = 프레임 폭. */
    val frameWidth: Float,
    /** 드래그 전 프레임 윗변 — 화면 중앙. */
    val baseFrameTop: Float,
    /** 프레임 윗변이 올라갈 수 있는 최솟값(화면·사진 위쪽 경계). */
    val minFrameTop: Float,
    /** 프레임 윗변이 내려갈 수 있는 최댓값(화면·사진 아래쪽 경계). */
    val maxFrameTop: Float,
) {
    /** 화면에 그려진 사진 높이(표시 px). */
    val displayedHeight: Float get() = imageHeightPx * imageScale

    /**
     * 프레임을 움직일 여지가 있는지.
     *
     * 프레임이 화면보다 높으면 [maxFrameTop] 이 [minFrameTop] 보다 작아진다. 이때
     * `coerceIn` 을 그대로 부르면 `IllegalArgumentException` 이 나므로 드래그를 막는다.
     */
    val hasRoom: Boolean get() = maxFrameTop >= minFrameTop

    /** 드래그 오프셋의 하한/상한. [hasRoom] 이 false 면 의미가 없다. */
    val minOffset: Float get() = minFrameTop - baseFrameTop
    val maxOffset: Float get() = maxFrameTop - baseFrameTop
}

/**
 * 화면·사진·프레임 비율로 배치를 계산한다.
 *
 * 사진은 화면 중앙에 고정하고 프레임이 위아래로 움직인다 — 반대로 하면 사진이 화면을
 * 벗어난 뒤 빈 영역이 보인다.
 *
 * 배율은 "화면 폭 맞춤"과 "프레임 덮기" 중 큰 쪽이다. 폭 맞춤만 쓰면 가로 사진(회전한
 * 세로 사진 포함)이 1:1 프레임보다 짧아 프레임 안에 검은 띠가 생기고, 그 띠는 업로드
 * 결과에 없는 영역이라 보이는 것과 저장되는 것이 어긋난다. 덮는 배율로 키우면 좌우가
 * 화면 밖으로 넘치므로 가운데를 남기고 x 방향도 잘라낸다.
 *
 * @param imageWidth 회전을 반영한 사진 폭(px). [rotatedSize] 참고.
 * @param frameRatio 프레임 높이/너비. 1 보다 크면 프레임이 화면 폭보다 높아진다.
 */
internal fun cropLayout(
    screenWidth: Float,
    screenHeight: Float,
    imageWidth: Int,
    imageHeight: Int,
    frameRatio: Float,
): CropLayout {
    val frameHeight = screenWidth * frameRatio
    val scale = maxOf(screenWidth / imageWidth, frameHeight / imageHeight)
    val displayedWidth = imageWidth * scale
    val displayedHeight = imageHeight * scale
    val imageTop = (screenHeight - displayedHeight) / 2f
    val imageBottom = imageTop + displayedHeight

    return CropLayout(
        frameHeight = frameHeight,
        imageLeft = (screenWidth - displayedWidth) / 2f,
        imageTop = imageTop,
        imageScale = scale,
        imageWidthPx = imageWidth,
        imageHeightPx = imageHeight,
        frameWidth = screenWidth,
        baseFrameTop = (screenHeight - frameHeight) / 2f,
        // 프레임은 화면 밖으로도, 사진 밖으로도 나가지 않는다. 둘 중 안쪽 경계를 쓴다.
        minFrameTop = maxOf(0f, imageTop),
        maxFrameTop = minOf(screenHeight - frameHeight, imageBottom - frameHeight),
    )
}

/** 드래그 오프셋을 실제 프레임 윗변으로. 여지가 없으면 중앙에 고정된다. */
internal fun CropLayout.frameTopAt(offset: Float): Float =
    if (hasRoom) (baseFrameTop + offset).coerceIn(minFrameTop, maxFrameTop) else baseFrameTop

/**
 * 프레임 위치(표시 좌표)를 원본 대비 정규화 크롭 영역으로 환산한다.
 *
 * 표시 좌표 → 원본 px 는 [CropLayout.imageScale] 로 나누고, 원본 px → 비율은 원본
 * 크기로 나눈다. 프레임이 화면보다 높아 사진 밖으로 삐져나온 경우 0~1 을 벗어나므로
 * 잘라낸다 — 그대로 넘기면 서버·뷰어 쪽에서 어떻게 해석될지 모른다.
 */
internal fun CropLayout.cropRegionAt(frameTop: Float): CropRegion {
    val x = (-imageLeft / imageScale / imageWidthPx).coerceIn(0f, 1f)
    val width = (frameWidth / imageScale / imageWidthPx).coerceIn(0f, 1f - x)
    val y = ((frameTop - imageTop) / imageScale / imageHeightPx).coerceIn(0f, 1f)
    val height = (frameHeight / imageScale / imageHeightPx).coerceIn(0f, 1f - y)
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
