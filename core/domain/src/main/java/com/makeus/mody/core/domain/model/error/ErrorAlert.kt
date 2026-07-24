package com.makeus.mody.core.domain.model.error

import java.io.IOException

/** 에러 다이얼로그 표시용 제목/본문 한 쌍. */
data class ErrorAlert(
    val title: String,
    val message: String,
)

/**
 * 예외 → 에러 다이얼로그 문구 매핑. 화면 공통 규칙:
 * - 5xx: 서버 문제로 안내 (서버 message 는 기술 문자열일 수 있어 노출하지 않음)
 * - 네트워크(IOException): 연결 확인 안내
 * - 그 외(4xx/논리 실패 등): 화면별 [fallbackTitle] + 서버 message(없으면 폴백 본문)
 *
 * CancellationException 은 호출 측에서 먼저 rethrow 할 것.
 */
fun Throwable.toErrorAlert(fallbackTitle: String = "요청에 실패했어요"): ErrorAlert = when {
    this is HttpResponseException && status.code >= 500 ->
        ErrorAlert(title = "서버에 문제가 생겼어요", message = "잠시 후 다시 시도해주세요.")

    this is HttpResponseException ->
        ErrorAlert(title = fallbackTitle, message = msg ?: "다시 시도해주세요.")

    this is IOException ->
        ErrorAlert(title = "네트워크 연결이 불안정해요", message = "연결 상태를 확인한 뒤 다시 시도해주세요.")

    else -> ErrorAlert(title = fallbackTitle, message = "다시 시도해주세요.")
}
