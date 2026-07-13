package com.makeus.mody.core.network.model

import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import kotlinx.serialization.Serializable

/**
 * 서버 공통 응답 봉투. 실제 페이로드는 [result].
 * 예: { "isSuccess": true, "code": "COMMON200", "message": "성공", "result": {...} }
 */
@Serializable
data class ApiResponse<T>(
    val isSuccess: Boolean = false,
    val code: String? = null,
    val message: String? = null,
    val result: T? = null,
)

@Serializable
data class ApiErrorResponse(
    val isSuccess: Boolean = false,
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
 * DataSource 에서 봉투 벗겨 순수 result 반환.
 * HTTP 200 이어도 isSuccess=false 면 실패로 간주해 [HttpResponseException] 을 던진다.
 * (non-2xx 는 CallAdapter 가 이미 같은 예외로 던지므로, 두 경로가 동일 예외로 일원화된다.
 *  → ViewModel 은 `(e as? HttpResponseException)?.msg` 하나로 서버 message 를 노출 가능.)
 * result 가 null 이면 (바디 없는 성공) Unit 으로 취급.
 */
@Suppress("UNCHECKED_CAST")
fun <T> ApiResponse<T>.unwrapResult(): T {
    if (!isSuccess) throw HttpResponseException(
        status = HttpResponseStatus.Ok, // 전송은 200이나 논리적 실패
        errorCode = ModyErrorCode.create(code),
        msg = message ?: code ?: "API 응답 실패",
    )
    return result ?: Unit as T
}
