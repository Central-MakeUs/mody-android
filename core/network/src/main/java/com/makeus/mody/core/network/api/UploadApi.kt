package com.makeus.mody.core.network.api

import com.makeus.mody.core.network.model.ApiResponse
import com.makeus.mody.core.network.model.record.PresignedUrlResponse
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 이미지 업로드 공통 API. 도메인(record/profile 등)별로 presigned URL 을 발급받아
 * S3 에 직접 PUT 한 뒤 imageKey 로 참조한다. (multipart 없음)
 */
interface UploadApi {

    /**
     * 이미지 업로드용 presigned URL 발급.
     * @param domain 업로드 도메인(예: "record", "profile"). imageKey 접두 경로가 결정된다.
     * @param fileName 확장자로 imageKey 확장자가 결정된다 (jpg/jpeg/png/webp).
     */
    @POST("api/v1/uploads/presigned-url")
    suspend fun createPresignedUrl(
        @Query("domain") domain: String,
        @Query("fileName") fileName: String,
    ): ApiResponse<PresignedUrlResponse>
}
