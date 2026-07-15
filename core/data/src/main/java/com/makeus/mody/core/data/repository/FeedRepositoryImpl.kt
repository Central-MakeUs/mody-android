package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ActivityCalendar
import com.makeus.mody.core.domain.model.ActivityDay
import com.makeus.mody.core.domain.model.Comment
import com.makeus.mody.core.domain.model.CommentPage
import com.makeus.mody.core.domain.model.FeedRecord
import com.makeus.mody.core.domain.model.FeedRecordPage
import com.makeus.mody.core.domain.model.RecordDetail
import com.makeus.mody.core.domain.model.RecordType
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.network.api.FeedApi
import com.makeus.mody.core.network.model.feed.ActivityCalendarResponse
import com.makeus.mody.core.network.model.feed.CommentCreateRequest
import com.makeus.mody.core.network.model.feed.CommentCursorResponse
import com.makeus.mody.core.network.model.feed.RecordCursorResponse
import com.makeus.mody.core.network.model.feed.RecordDetailItemResponse
import com.makeus.mody.core.network.model.feed.RecordDetailResponse
import com.makeus.mody.core.network.model.feed.RecordSummaryResponse
import com.makeus.mody.core.network.model.unwrapResult
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val feedApi: FeedApi,
) : FeedRepository {

    override suspend fun getActivityCalendar(groupId: Long, baseDate: LocalDate): ActivityCalendar =
        feedApi.getActivityCalendar(groupId = groupId, baseDate = baseDate.toString())
            .unwrapResult()
            .toActivityCalendar()

    override suspend fun getRecords(
        groupId: Long,
        date: LocalDate,
        cursor: Long?,
        size: Int?,
    ): FeedRecordPage =
        feedApi.getRecords(groupId = groupId, date = date.toString(), cursor = cursor, size = size)
            .unwrapResult()
            .toFeedRecordPage()

    override suspend fun getRecordDetail(
        groupId: Long,
        recordId: Long,
        cursor: Long?,
        size: Int?,
    ): RecordDetail =
        feedApi.getRecordDetail(groupId = groupId, recordId = recordId, cursor = cursor, size = size)
            .unwrapResult()
            .toRecordDetail()

    override suspend fun getComments(
        groupId: Long,
        recordId: Long,
        cursor: Long?,
        size: Int?,
    ): CommentPage =
        feedApi.getComments(groupId = groupId, recordId = recordId, cursor = cursor, size = size)
            .unwrapResult()
            .toCommentPage()

    override suspend fun postComment(groupId: Long, recordId: Long, content: String): Long =
        feedApi.postComment(
            groupId = groupId,
            recordId = recordId,
            request = CommentCreateRequest(content = content),
        ).unwrapResult().commentId
}

private fun ActivityCalendarResponse.toActivityCalendar(): ActivityCalendar =
    ActivityCalendar(
        weekStartDate = LocalDate.parse(weekStartDate),
        weekEndDate = LocalDate.parse(weekEndDate),
        days = days.map { ActivityDay(date = LocalDate.parse(it.date), hasRecord = it.hasRecord) },
    )

private fun RecordCursorResponse.toFeedRecordPage(): FeedRecordPage =
    FeedRecordPage(
        records = records.map { it.toFeedRecord() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

private fun RecordSummaryResponse.toFeedRecord(): FeedRecord =
    FeedRecord(
        recordId = recordId,
        type = recordType.toRecordType(),
        memberId = memberId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        imageUrl = imageUrl,
        streakDays = recordingStreakDays,
        recordedTime = recordedTime?.takeIf { it.isNotBlank() }?.let(LocalTime::parse),
        menu = menu,
        exerciseDurationMinutes = exerciseDurationMinutes,
        exerciseName = exerciseName,
    )

private fun RecordDetailResponse.toRecordDetail(): RecordDetail =
    RecordDetail(
        totalCount = totalCount,
        currentIndex = currentIndex,
        records = records.map { it.toFeedRecord() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

// 상세 항목엔 recordingStreakDays 가 없어 streakDays=0.
private fun RecordDetailItemResponse.toFeedRecord(): FeedRecord =
    FeedRecord(
        recordId = recordId,
        type = recordType.toRecordType(),
        memberId = memberId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        imageUrl = imageUrl,
        streakDays = 0,
        recordedTime = recordedTime?.takeIf { it.isNotBlank() }?.let(LocalTime::parse),
        menu = menu,
        exerciseDurationMinutes = exerciseDurationMinutes,
        exerciseName = exerciseName,
    )

private fun CommentCursorResponse.toCommentPage(): CommentPage =
    CommentPage(
        comments = comments.map {
            Comment(
                commentId = it.commentId,
                memberId = it.memberId,
                nickname = it.nickname,
                profileImageUrl = it.profileImageUrl,
                content = it.content,
                isMine = it.isMine,
            )
        },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

private fun String.toRecordType(): RecordType = when (this) {
    "MEAL" -> RecordType.MEAL
    "EXERCISE" -> RecordType.EXERCISE
    else -> RecordType.UNKNOWN
}
