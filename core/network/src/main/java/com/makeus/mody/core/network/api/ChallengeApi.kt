package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.challenge.ChallengeSummaryResponse
import com.makeus.mody.core.network.model.challenge.NudgeResponse
import com.makeus.mody.core.network.model.challenge.NudgeTargetListResponse
import com.makeus.mody.core.network.model.challenge.StepChallengeChangeRequest
import com.makeus.mody.core.network.model.challenge.StepChallengeChangeResponse
import com.makeus.mody.core.network.model.challenge.StepChallengeOptionListResponse
import com.makeus.mody.core.network.model.challenge.StepChallengeStatusResponse
import com.makeus.mody.core.network.model.challenge.StepRankingListResponse
import com.makeus.mody.core.network.model.challenge.StepRecordUpsertRequest
import com.makeus.mody.core.network.model.challenge.StepRecordUpsertResponse
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeListResponse
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeProofCreateRequest
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeProofCreateResponse
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeProofListResponse
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeShareResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    /** 콕 찌르기(푸시 발송). 응답으로 찌른 뒤의 버튼 상태를 준다. */
    @POST("api/v1/groups/{groupId}/challenges/nudges/{memberId}")
    suspend fun nudge(
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long,
    ): ApiResponse<NudgeResponse>

    /** 진행 중인 그룹 필수(걸음 수) 챌린지 현황. */
    @GET("api/v1/groups/{groupId}/challenges/step/current")
    suspend fun getStepChallenge(
        @Path("groupId") groupId: Long,
    ): ApiResponse<StepChallengeStatusResponse>

    /** 오늘 걸음 수 반영(같은 날짜 재전송 시 덮어쓰기). */
    @PUT("api/v1/groups/{groupId}/challenges/step/records")
    suspend fun upsertStepRecord(
        @Path("groupId") groupId: Long,
        @Body request: StepRecordUpsertRequest,
    ): ApiResponse<StepRecordUpsertResponse>

    /** 선택 가능한 걸음 수 챌린지 목록(챌린지 변경 화면). */
    @GET("api/v1/groups/{groupId}/challenges/step/options")
    suspend fun getStepChallengeOptions(
        @Path("groupId") groupId: Long,
    ): ApiResponse<StepChallengeOptionListResponse>

    /** 진행 중인 걸음 수 챌린지 교체. 기존 걸음 기록은 서버에서 초기화된다. */
    @PATCH("api/v1/groups/{groupId}/challenges/step/current")
    suspend fun changeStepChallenge(
        @Path("groupId") groupId: Long,
        @Body request: StepChallengeChangeRequest,
    ): ApiResponse<StepChallengeChangeResponse>

    /** 걸음 수 기여도 순위. */
    @GET("api/v1/groups/{groupId}/challenges/step/rankings")
    suspend fun getStepRankings(
        @Path("groupId") groupId: Long,
    ): ApiResponse<StepRankingListResponse>

    /** 이번주 그룹 선택(주간) 챌린지 목록. */
    @GET("api/v1/groups/{groupId}/challenges/weekly")
    suspend fun getWeeklyChallenges(
        @Path("groupId") groupId: Long,
    ): ApiResponse<WeeklyChallengeListResponse>

    /** 주간 챌린지 인증 사진 목록. */
    @GET("api/v1/groups/{groupId}/weekly-challenges/{groupChallengeId}/proofs")
    suspend fun getWeeklyChallengeProofs(
        @Path("groupId") groupId: Long,
        @Path("groupChallengeId") groupChallengeId: Long,
    ): ApiResponse<WeeklyChallengeProofListResponse>

    /** 인증 사진 등록. 이미지는 presigned 업로드 후 받은 imageKey 로 참조한다. */
    @POST("api/v1/groups/{groupId}/weekly-challenges/{groupChallengeId}/proofs")
    suspend fun createWeeklyChallengeProof(
        @Path("groupId") groupId: Long,
        @Path("groupChallengeId") groupChallengeId: Long,
        @Body request: WeeklyChallengeProofCreateRequest,
    ): ApiResponse<WeeklyChallengeProofCreateResponse>

    /** 공유용 콜라주 이미지 생성. 서버가 인증 사진들을 합쳐 한 장으로 준다. */
    @POST("api/v1/groups/{groupId}/weekly-challenges/{groupChallengeId}/share")
    suspend fun shareWeeklyChallenge(
        @Path("groupId") groupId: Long,
        @Path("groupChallengeId") groupChallengeId: Long,
    ): ApiResponse<WeeklyChallengeShareResponse>
}
