package com.makeus.mody.core.network.calladapter

import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * API 실패 중 "개발자가 알아야 하는 것"만 고른다.
 *
 * 전부 보내면 지하철에서 난 타임아웃으로 대시보드가 덮여 아무도 안 본다. 사용자 환경이나
 * 정상 흐름에서 나오는 실패는 버리고, 앱/서버가 어긋났다는 신호만 남긴다.
 */
internal fun Throwable.isWorthReporting(): Boolean = when (this) {
    // 스펙 불일치. 앱은 빈 화면이 되는데 크래시도 안 나 제일 안 보이는 종류다. 최우선 보고 대상.
    is SerializationException -> true

    // 오프라인·타임아웃·DNS. 앱 문제가 아니고 양도 많다.
    is IOException -> false

    is HttpResponseException -> when {
        // TokenAuthenticator 가 재발급으로 처리하는 정상 흐름.
        status == HttpResponseStatus.Unauthorized -> false
        status == HttpResponseStatus.Forbidden -> false

        // 서버 장애.
        status.code >= 500 -> true

        // 4xx 는 대부분 사용자 유발(중복 참여·잘못된 코드 등)이고 서버 msg 를 그대로 UI 에 띄운다.
        // 다만 앱이 모르는 에러 코드가 오면 스펙이 늘어난 것이므로 남긴다.
        status.code in 400..499 -> errorCode == ModyErrorCode.UNKNOWN

        // status 를 못 알아본 응답(Unknown) 등 예상 밖.
        else -> true
    }

    else -> true
}

/** Crashlytics custom key. 엔드포인트별 그룹핑에 쓴다. */
internal fun apiErrorContext(
    method: String,
    url: String,
    throwable: Throwable,
): Map<String, String> = buildMap {
    put("http_method", method)
    // 쿼리스트링에는 코드·토큰 같은 값이 섞일 수 있어 경로만 남긴다.
    put("endpoint", url.substringBefore('?'))
    (throwable as? HttpResponseException)?.let {
        put("http_status", it.status.code.toString())
        put("error_code", it.errorCode.name)
    }
}
