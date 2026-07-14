package com.makeus.mody.core.network.model.feed

import kotlinx.serialization.Serializable

/** 주간 기록 캘린더 응답. baseDate 가 속한 주(일~토)의 기록 유무. */
@Serializable
data class ActivityCalendarResponse(
    val weekStartDate: String = "",
    val weekEndDate: String = "",
    val days: List<ActivityDayResponse> = emptyList(),
)

@Serializable
data class ActivityDayResponse(
    val date: String = "",
    val dayOfWeek: String = "",
    val hasRecord: Boolean = false,
)

/** 날짜별 그룹 기록(피드) 목록. 커서 페이지네이션. */
@Serializable
data class RecordCursorResponse(
    val records: List<RecordSummaryResponse> = emptyList(),
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
)

/**
 * 피드 카드 한 건.
 * recordType = MEAL 이면 recordedTime/menu, EXERCISE 이면 exerciseDurationMinutes/exerciseName 채워짐.
 * recordedTime 은 "13:00:00" 형태의 ISO LocalTime 문자열.
 */
@Serializable
data class RecordSummaryResponse(
    val recordId: Long = 0,
    val recordType: String = "",
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val recordedTime: String? = null,
    val menu: String? = null,
    val exerciseDurationMinutes: Int? = null,
    val exerciseName: String? = null,
    val imageUrl: String? = null,
    val recordingStreakDays: Int = 0,
)
