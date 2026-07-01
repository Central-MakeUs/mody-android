package com.makeus.mody.core.network.model

import kotlinx.serialization.Serializable

/**
 * 서버 공통 응답 봉투. 실제 페이로드는 [data].
 * 백엔드 스펙에 맞춰 필드명 조정할 것.
 */
@Serializable
data class ApiResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null,
)

@Serializable
data class ApiErrorResponse(
    val code: String? = null,
    val message: String? = null,
    val errors: List<ErrorDetail>? = emptyList(),
)

@Serializable
data class ErrorDetail(
    val field: String? = null,
    val message: String? = null,
)

/**
 * DataSource 에서 봉투 벗겨 순수 data 반환.
 * data 가 null 이면 (204 등 바디 없는 성공) Unit 으로 취급.
 */
@Suppress("UNCHECKED_CAST")
fun <T> ApiResponse<T>.unwrapData(): T = data ?: Unit as T
