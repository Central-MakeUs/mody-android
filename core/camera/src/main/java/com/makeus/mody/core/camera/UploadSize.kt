package com.makeus.mody.core.camera

/**
 * 업로드본 크기 계산. 디코딩·인코딩과 떼어 둔 건 단위 테스트로 고정하기 위해서다 —
 * 한 번 틀리면 화면에선 멀쩡해 보이고 업로드본만 작아진다(실제로 그랬다).
 *
 * 줄이는 건 두 단계다.
 * 1. [decodeSampleSize] — 디코더가 픽셀을 건너뛰며 읽는다. 빠르고 메모리를 아끼지만 1/2, 1/4…
 *    단위로만 줄어 목표에 정확히 맞지 않는다.
 * 2. [downscaleFactor] — 1단계 결과를 목표 크기로 보간해 맞춘다.
 *
 * 1단계에서 목표보다 작게 내려가면 2단계로 되살릴 수 없으므로, 1단계는 목표 이상에서 멈춘다.
 */

/**
 * 장변이 [targetEdge] 아래로 내려가지 않는 가장 큰 2의 거듭제곱 배율.
 *
 * 결과 장변은 `[targetEdge, 2 * targetEdge)` 에 든다(원본이 그보다 작으면 원본 그대로).
 * 2배 미만에서 보간하므로 한 번의 bilinear 축소로도 앨리어싱이 거의 없다.
 */
internal fun decodeSampleSize(longestEdge: Int, targetEdge: Int): Int {
    var sample = 1
    while (longestEdge / (sample * 2) >= targetEdge) sample *= 2
    return sample
}

/** 장변을 [targetEdge] 로 맞추는 배율. 이미 작으면 키우지 않는다(1). */
internal fun downscaleFactor(longestEdge: Int, targetEdge: Int): Float =
    if (longestEdge <= targetEdge) 1f else targetEdge.toFloat() / longestEdge
