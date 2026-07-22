package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.group.CreateGroupRequest
import com.makeus.mody.core.network.model.group.GroupListResponse
import com.makeus.mody.core.network.model.group.GroupResponse
import com.makeus.mody.core.network.model.group.JoinGroupRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApi {

    @POST("api/v1/onboarding/groups")
    suspend fun createGroup(
        @Body request: CreateGroupRequest,
    ): ApiResponse<GroupResponse>

    @POST("api/v1/onboarding/groups/join")
    suspend fun joinGroup(
        @Body request: JoinGroupRequest,
    ): ApiResponse<GroupResponse>

    @GET("api/v1/groups")
    suspend fun getMyGroups(): ApiResponse<GroupListResponse>

    @DELETE("api/v1/groups/{groupId}/members/me")
    suspend fun leaveGroup(
        @Path("groupId") groupId: Long,
    ): ApiResponse<Unit>
}
