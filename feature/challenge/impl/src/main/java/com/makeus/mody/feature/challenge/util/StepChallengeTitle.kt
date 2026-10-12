package com.makeus.mody.feature.challenge.util

/**
 * 걸음 수 챌린지 제목 표시 문구.
 *
 * 서버는 `"출발지-도착지"` 형식으로 준다 — 현재 출발지가 서울 고정이라 실제로는
 * `"서울-인천"`, `"서울-부산"` 같은 값이다. 화면에는 `"서울에서 인천까지"` 로 보여준다.
 *
 * **하이픈이 정확히 하나이고 양쪽이 비어 있지 않을 때만 바꾼다.** 그 외에는 받은 값을
 * 그대로 돌려준다 — 서버가 형식을 바꾸거나 지역명 자체에 하이픈이 들어가면, 잘못 쪼개서
 * 이상한 문장을 만드는 것보다 원문을 보여주는 쪽이 낫다. 화면에서 원인도 바로 보인다.
 */
internal fun stepChallengeTitle(rawTitle: String): String {
    val parts = rawTitle.split(SEPARATOR)
    if (parts.size != 2) return rawTitle

    val departure = parts[0].trim()
    val destination = parts[1].trim()
    if (departure.isEmpty() || destination.isEmpty()) return rawTitle

    return "${departure}에서 ${destination}까지"
}

private const val SEPARATOR = '-'
