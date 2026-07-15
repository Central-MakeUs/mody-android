package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.record.PresignedUrlResponse
import com.makeus.mody.core.network.model.record.RecordCreateRequest
import com.makeus.mody.core.network.model.record.RecordCreateResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RecordApi {

    /**
     * 이미지 업로드용 presigned URL 발급.
     * @param domain 업로드 도메인. 기록 이미지는 "record" (→ imageKey `records/...`).
     * @param fileName 확장자로 imageKey 확장자가 결정된다 (jpg/jpeg/png/webp).
     */
    @POST("api/v1/uploads/presigned-url")
    suspend fun createPresignedUrl(
        @Query("domain") domain: String,
        @Query("fileName") fileName: String,
    ): ApiResponse<PresignedUrlResponse>

    /** 기록 생성 (사진은 imageKey 로 참조). */
    @POST("api/v1/records")
    suspend fun createRecord(
        @Body request: RecordCreateRequest,
    ): ApiResponse<RecordCreateResponse>
}
