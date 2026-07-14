package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.domain.model.FeedRecord
import com.makeus.mody.core.domain.model.RecordType
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 도메인 기록 → 카드 표시 모델. 식사/운동에 따라 라벨·값 구성. */
fun FeedRecord.toFeedCardUi(): FeedCardUi = when (type) {
    RecordType.MEAL -> FeedCardUi(
        id = recordId,
        authorName = nickname,
        dayCount = streakDays,
        primaryLabel = "식사 시간",
        primaryValue = recordedTime?.format(TIME_FORMAT).orEmpty(),
        secondaryLabel = "메뉴",
        secondaryValue = menu.orEmpty(),
        avatarUrl = profileImageUrl,
        imageUrl = imageUrl,
    )
    else -> FeedCardUi(
        id = recordId,
        authorName = nickname,
        dayCount = streakDays,
        primaryLabel = "운동 시간",
        primaryValue = exerciseDurationMinutes?.let { "${it}분" }.orEmpty(),
        secondaryLabel = "운동종류",
        secondaryValue = exerciseName.orEmpty(),
        avatarUrl = profileImageUrl,
        imageUrl = imageUrl,
    )
}
