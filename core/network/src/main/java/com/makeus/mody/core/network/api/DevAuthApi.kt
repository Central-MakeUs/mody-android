package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.auth.CreateMockMemberRequest
import com.makeus.mody.core.network.model.auth.DevTokenResponse
import com.makeus.mody.core.network.model.auth.IssueTokenRequest
import com.makeus.mody.core.network.model.auth.MockMemberResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 스토어 심사용 dev 툴 API.
 * 소셜 계정 없이 목 멤버를 만들고 토큰을 발급받아 로그인한다.
 * 로그인 화면 히든 진입(로고 연타 + 비밀번호)에서만 사용.
 */
interface DevAuthApi {

    @POST("api/v1/dev/members/mock")
    suspend fun createMockMember(
        @Body request: CreateMockMemberRequest,
    ): ApiResponse<MockMemberResponse>

    @POST("api/v1/dev/auth/tokens")
    suspend fun issueToken(
        @Body request: IssueTokenRequest,
    ): ApiResponse<DevTokenResponse>
}
