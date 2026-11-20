package com.makeus.mody.core.network.model.feed

import com.makeus.mody.core.network.model.record.ImageCropRegionDto
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
    val imageCropRegion: ImageCropRegionDto? = null,
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
    val imageCropRegion: ImageCropRegionDto? = null,
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
    /** 이모지 하나만 있는 댓글이면 그 아이디(clap·medal·fire·heart·run). 아니면 null. */
    val emojiId: String? = null,
    /** 이모지 하나만 있는 댓글. 이때 [content] 는 빈 문자열이고 [emojiId] 를 크게 그린다. */
    val emojiOnly: Boolean = false,
    /** 작성 시각(ISO-8601). 오프셋이 없으면 KST. */
    val createdAt: String? = null,
    val isMine: Boolean = false,
)

@Serializable
data class CommentCreateRequest(
    /** 글과 이모지 토큰(`:mody-clap:`)을 섞은 본문. 이모지 하나만 보낼 때도 토큰 하나로 보낸다. 최대 500자. */
    val content: String,
)

@Serializable
data class CommentCreateResponse(
    val commentId: Long = 0,
    val recordId: Long = 0,
)

/** 기록(게시물) 신고 접수 응답. */
@Serializable
data class RecordReportResponse(
    val reportId: Long = 0,
    val recordId: Long = 0,
)
