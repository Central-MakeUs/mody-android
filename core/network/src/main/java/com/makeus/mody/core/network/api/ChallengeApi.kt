package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.challenge.ChallengeSummaryResponse
import com.makeus.mody.core.network.model.challenge.NudgeTargetListResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChallengeApi {

    /** 연속 기록 탭 상단 요약(전원 연속 기록 일수·D+N·운동시간·챌린지 개수). */
    @GET("api/v1/groups/{groupId}/challenges/summary")
    suspend fun getSummary(
        @Path("groupId") groupId: Long,
    ): ApiResponse<ChallengeSummaryResponse>

    /** 버디 신기록 도전 목록(멤버별 오늘 기록 여부). */
    @GET("api/v1/groups/{groupId}/challenges/nudges")
    suspend fun getNudgeTargets(
        @Path("groupId") groupId: Long,
    ): ApiResponse<NudgeTargetListResponse>

    /** 콕 찌르기(푸시 발송). */
    @POST("api/v1/groups/{groupId}/challenges/nudges/{memberId}")
    suspend fun nudge(
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long,
    ): ApiResponse<Unit>
}
