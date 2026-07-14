package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.ActivityCalendar
import com.makeus.mody.core.domain.model.FeedRecordPage
import java.time.LocalDate

/** 피드 화면 데이터. */
interface FeedRepository {
    /** baseDate 가 속한 주(일~토)의 그룹 기록 유무 캘린더. */
    suspend fun getActivityCalendar(groupId: Long, baseDate: LocalDate): ActivityCalendar

    /** 특정 날짜의 그룹 기록(피드) 목록. cursor=null 이면 첫 페이지. */
    suspend fun getRecords(
        groupId: Long,
        date: LocalDate,
        cursor: Long? = null,
        size: Int? = null,
    ): FeedRecordPage
}
