package com.makeus.mody.core.domain.model.error

/**
 * 서버/네트워크 실패를 도메인 계층 예외로 표준화.
 * CallAdapter 가 HTTP errorBody 를 이걸로 변환해 던진다.
 */
data class HttpResponseException(
    val status: HttpResponseStatus,
    val errorCode: ModyErrorCode,
    val msg: String? = null,
    override val cause: Throwable? = null,
) : Exception(msg, cause)

enum class HttpResponseStatus(val code: Int, val msg: String) {
    Ok(200, "Ok"),
    Created(201, "Created"),
    NoContent(204, "No Content"),
    BadRequest(400, "Bad Request"),
    Unauthorized(401, "Unauthorized"),
    Forbidden(403, "Forbidden"),
    NotFound(404, "Not Found"),
    Conflict(409, "Conflict"),
    UnProcessableEntity(422, "Unprocessable Entity"),
    ReqTooMany(429, "Req Too Many"),
    InternalError(500, "Internal Error"),
    BadGateway(502, "Bad Gateway"),
    Unavailable(503, "Unavailable"),
    GatewayTimeout(504, "Gateway Timeout"),
    Unknown(-1, "Unknown"),
    ;

    companion object {
        fun create(code: Int): HttpResponseStatus =
            entries.firstOrNull { it.code == code } ?: Unknown
    }

    override fun toString(): String = "$code $msg"
}

/**
 * 서버가 내려주는 에러 코드 문자열 → enum.
 * 백엔드 스펙 확정되면 케이스 추가. 매칭 실패 시 UNKNOWN.
 */
enum class ModyErrorCode(val msg: String) {
    INVALID_PARAMETER("잘못된 요청입니다."),
    UNAUTHORIZED("인증이 필요합니다."),
    FORBIDDEN("접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND("대상을 찾을 수 없습니다."),
    EXPIRED_ACCESS_TOKEN("토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN("유효하지 않은 토큰입니다."),
    INTERNAL_SERVER_ERROR("서버 오류입니다."),
    UNKNOWN("알 수 없는 오류입니다."),
    ;

    companion object {
        fun create(value: String?): ModyErrorCode =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
