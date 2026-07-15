package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ActivityCalendar
import com.makeus.mody.core.domain.model.ActivityDay
import com.makeus.mody.core.domain.model.FeedRecord
import com.makeus.mody.core.domain.model.FeedRecordPage
import com.makeus.mody.core.domain.model.RecordType
import com.makeus.mody.core.domain.repository.FeedRepository
import com.makeus.mody.core.network.api.FeedApi
import com.makeus.mody.core.network.model.feed.ActivityCalendarResponse
import com.makeus.mody.core.network.model.feed.RecordCursorResponse
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
        type = when (recordType) {
            "MEAL" -> RecordType.MEAL
            "EXERCISE" -> RecordType.EXERCISE
            else -> RecordType.UNKNOWN
        },
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
