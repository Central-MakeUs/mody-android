package com.makeus.mody.core.data.repository

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

/** 서버가 타임존 없는 문자열을 줄 때 적용할 기준. 서비스가 국내 전용이라 KST 고정. */
private val ServerZone: ZoneId = ZoneId.of("Asia/Seoul")

/**
 * 서버 날짜+시각 문자열 → [Instant].
 *
 * 포맷 계약이 확정되지 않아 세 형태를 모두 받는다.
 *  - `2026-08-06T14:40:00+09:00` / `...Z` : 오프셋 그대로 사용
 *  - `2026-08-06T14:40:00`                : 오프셋 없음 → KST 로 해석
 *  - `2026-08-06 14:40:00`                : 공백 구분 → 위와 동일
 *
 * 파싱 실패는 null. 호출부가 기본값(예: 오늘 0시)으로 처리한다 — 시각 하나 때문에
 * 화면 전체를 실패시키지 않는다.
 */
internal fun String?.toServerInstantOrNull(): Instant? {
    val text = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalized = text.replace(' ', 'T')
    runCatching { return OffsetDateTime.parse(normalized).toInstant() }
    runCatching { return LocalDateTime.parse(normalized).atZone(ServerZone).toInstant() }
    return null
}
