package com.makeus.mody.core.domain.model

import java.time.LocalTime

enum class RecordType { MEAL, EXERCISE, UNKNOWN }

/**
 * 피드 카드 한 건.
 * MEAL 이면 recordedTime/menu, EXERCISE 이면 exerciseDurationMinutes/exerciseName 유효.
 */
data class FeedRecord(
    val recordId: Long,
    val type: RecordType,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val imageUrl: String?,
    /** 원본 이미지에서 표시할 크롭 영역(정규화 0~1). null 이면 원본 전체 표시(예: 갤러리 선택). */
    val cropRegion: CropRegion?,
    val streakDays: Int,
    // MEAL
    val recordedTime: LocalTime?,
    val menu: String?,
    // EXERCISE
    val exerciseDurationMinutes: Int?,
    val exerciseName: String?,
)

/** 커서 페이지네이션 결과. */
data class FeedRecordPage(
    val records: List<FeedRecord>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
