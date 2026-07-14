package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.feed.ActivityCalendarResponse
import com.makeus.mody.core.network.model.feed.CommentCreateRequest
import com.makeus.mody.core.network.model.feed.CommentCreateResponse
import com.makeus.mody.core.network.model.feed.CommentCursorResponse
import com.makeus.mody.core.network.model.feed.RecordCursorResponse
import com.makeus.mody.core.network.model.feed.RecordDetailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApi {

    /** baseDate 가 속한 주(일~토)의 그룹 기록 유무 캘린더. */
    @GET("api/v1/groups/{groupId}/activities/calendar")
    suspend fun getActivityCalendar(
        @Path("groupId") groupId: Long,
        @Query("baseDate") baseDate: String,
    ): ApiResponse<ActivityCalendarResponse>

    /** 특정 날짜의 그룹 기록(피드) 목록. */
    @GET("api/v1/groups/{groupId}/records")
    suspend fun getRecords(
        @Path("groupId") groupId: Long,
        @Query("date") date: String,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<RecordCursorResponse>

    /** 기록 상세: 탭한 기록부터 좌우로 넘겨보는 슬라이드. */
    @GET("api/v1/groups/{groupId}/records/{recordId}")
    suspend fun getRecordDetail(
        @Path("groupId") groupId: Long,
        @Path("recordId") recordId: Long,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<RecordDetailResponse>

    /** 기록 댓글 목록. */
    @GET("api/v1/groups/{groupId}/records/{recordId}/comments")
    suspend fun getComments(
        @Path("groupId") groupId: Long,
        @Path("recordId") recordId: Long,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<CommentCursorResponse>

    /** 기록에 댓글 작성. */
    @POST("api/v1/groups/{groupId}/records/{recordId}/comments")
    suspend fun postComment(
        @Path("groupId") groupId: Long,
        @Path("recordId") recordId: Long,
        @Body request: CommentCreateRequest,
    ): ApiResponse<CommentCreateResponse>
}
