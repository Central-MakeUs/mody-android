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

/**
 * 기록 상세. 탭한 기록부터 좌우로 넘겨보는 슬라이드.
 * currentIndex = 진입 시 보여줄 위치, totalCount = 전체 개수, records = 커서로 이어붙임.
 */
@Serializable
data class RecordDetailResponse(
    val totalCount: Int = 0,
    val currentIndex: Int = 0,
    val records: List<RecordDetailItemResponse> = emptyList(),
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
)

/** 상세 슬라이드 한 건. 목록과 동일하나 recordingStreakDays 없음. recordedTime 은 ISO 문자열. */
@Serializable
data class RecordDetailItemResponse(
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
)

/** 기록 댓글 목록. 커서 페이지네이션. */
@Serializable
data class CommentCursorResponse(
    val comments: List<CommentResponse> = emptyList(),
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,
)

@Serializable
data class CommentResponse(
    val commentId: Long = 0,
    val memberId: Long = 0,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val content: String = "",
    val isMine: Boolean = false,
)

@Serializable
data class CommentCreateRequest(
    val content: String,
)

@Serializable
data class CommentCreateResponse(
    val commentId: Long = 0,
    val recordId: Long = 0,
)
