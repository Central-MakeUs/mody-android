package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.ActivityCalendar
import com.makeus.mody.core.domain.model.CommentPage
import com.makeus.mody.core.domain.model.FeedRecordPage
import com.makeus.mody.core.domain.model.RecordDetail
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

    /** 기록 상세(좌우 슬라이드). cursor=null 이면 첫 페이지. */
    suspend fun getRecordDetail(
        groupId: Long,
        recordId: Long,
        cursor: Long? = null,
        size: Int? = null,
    ): RecordDetail

    /** 기록 댓글 목록. cursor=null 이면 첫 페이지. */
    suspend fun getComments(
        groupId: Long,
        recordId: Long,
        cursor: Long? = null,
        size: Int? = null,
    ): CommentPage

    /** 기록에 댓글 작성 후, 생성된 댓글 id. */
    suspend fun postComment(groupId: Long, recordId: Long, content: String): Long
}
