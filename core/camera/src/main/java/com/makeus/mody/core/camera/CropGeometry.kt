package com.makeus.mody.core.camera

import com.makeus.mody.core.domain.model.CropRegion

/**
 * 조정 화면의 프레임·사진 배치와 크롭 영역 계산.
 *
 * Composable 밖으로 빼둔 이유는 두 가지다. 하나는 화면 크기·사진 비율 조합마다
 * 손으로 확인할 수 없어서고(세로 사진·가로 사진·프레임보다 짧은 사진이 각각 다르게
 * 동작한다), 다른 하나는 [CropRegion] 이 원본 대비 정규화 비율이라 여기서 한 번
 * 틀리면 업로드된 사진이 엉뚱하게 잘려도 화면에선 안 보이기 때문이다.
 *
 * 좌표계: 표시 좌표(px), 위에서 아래로 증가. 세로 슬라이스 전용이라 가로는 항상
 * 화면 폭을 꽉 채운다(x=0, width=1).
 */
internal data class CropLayout(
    /** 프레임 높이(표시 px). */
    val frameHeight: Float,
    /** 화면에 그려진 사진의 윗변(표시 px). 사진은 중앙 고정이라 음수가 될 수 있다. */
    val imageTop: Float,
    /** 원본 px → 표시 px 배율. 프레임 폭이 화면 폭이므로 `화면 폭 / 원본 폭`. */
    val imageScale: Float,
    /** 원본 사진 높이(px). 정규화에 쓴다. */
    val imageHeightPx: Int,
    /** 드래그 전 프레임 윗변 — 화면 중앙. */
    val baseFrameTop: Float,
    /** 프레임 윗변이 내려갈 수 있는 최솟값(화면·사진 위쪽 경계). */
    val minFrameTop: Float,
    /** 프레임 윗변이 내려갈 수 있는 최댓값(화면·사진 아래쪽 경계). */
    val maxFrameTop: Float,
) {
    /**
     * 프레임을 움직일 여지가 있는지.
     *
     * 사진이 프레임보다 짧으면 [maxFrameTop] 이 [minFrameTop] 보다 작아진다. 이때
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
    val scale = screenWidth / imageWidth
    val displayedHeight = imageHeight * scale
    val imageTop = (screenHeight - displayedHeight) / 2f
    val imageBottom = imageTop + displayedHeight

    return CropLayout(
        frameHeight = frameHeight,
        imageTop = imageTop,
        imageScale = scale,
        imageHeightPx = imageHeight,
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
 * 높이로 나눈다. 사진이 프레임보다 짧아 프레임이 사진 밖으로 삐져나온 경우 y/height 가
 * 0~1 을 벗어나므로 잘라낸다 — 그대로 넘기면 서버·뷰어 쪽에서 어떻게 해석될지 모른다.
 */
internal fun CropLayout.cropRegionAt(frameTop: Float): CropRegion {
    val topInImage = (frameTop - imageTop) / imageScale
    val heightInImage = frameHeight / imageScale
    val y = (topInImage / imageHeightPx).coerceIn(0f, 1f)
    val height = (heightInImage / imageHeightPx).coerceIn(0f, 1f - y)
    return CropRegion(x = 0f, y = y, width = 1f, height = height)
}
