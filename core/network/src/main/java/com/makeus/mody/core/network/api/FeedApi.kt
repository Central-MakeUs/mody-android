package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.feed.ActivityCalendarResponse
import com.makeus.mody.core.network.model.feed.RecordCursorResponse
import retrofit2.http.GET
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
}
