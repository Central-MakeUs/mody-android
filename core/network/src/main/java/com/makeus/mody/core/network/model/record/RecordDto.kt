package com.makeus.mody.core.network.model.record

import kotlinx.serialization.Serializable

/** presigned URL 발급 응답: S3 PUT 대상 URL 과, 기록 생성에 넘길 imageKey. */
@Serializable
data class PresignedUrlResponse(
    val presignedUrl: String = "",
    val imageKey: String = "",
    val expiresInSeconds: Long = 0,
)

/**
 * 기록 생성 요청.
 * MEAL: mealTime + menu 사용. EXERCISE: exercise* 필드 사용 (여기선 MEAL 만 채운다).
 * null 필드는 encodeDefaults=false 로 직렬화에서 제외된다.
 */
@Serializable
data class RecordCreateRequest(
    val recordType: String,
    val imageKey: String,
    val mealTime: String? = null, // "HH:mm:ss" (서버 LocalTime)
    val menu: String? = null,
    val exerciseDurationHours: Int? = null,
    val exerciseDurationMinutes: Int? = null,
    val exerciseName: String? = null,
)

@Serializable
data class RecordCreateResponse(
    val recordId: Long = 0,
    val groupIds: List<Long> = emptyList(),
)
