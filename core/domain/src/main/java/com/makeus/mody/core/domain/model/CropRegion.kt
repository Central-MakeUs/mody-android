package com.makeus.mody.core.domain.model

/**
 * 이미지 크롭 영역. 원본 이미지 대비 정규화 비율(0~1).
 * 원본을 그대로 업로드하고, 표시할 때 이 영역만 잘라 보여준다.
 * (현재 크롭 UI 는 세로 슬라이스 전용이라 x=0, width=1, y/height 만 변한다.)
 */
data class CropRegion(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)
