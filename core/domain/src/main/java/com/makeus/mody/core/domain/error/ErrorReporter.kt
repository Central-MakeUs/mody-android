package com.makeus.mody.core.domain.error

/**
 * 사용자에게는 조용히 넘어가지만 개발자는 알아야 하는 실패를 남긴다(non-fatal).
 *
 * 이 앱은 실패를 대부분 흡수한다 — 조회가 실패해도 이전 값을 유지하거나 빈 화면을 보여준다.
 * 그래서 서버 스펙이 바뀌거나 특정 API 가 계속 죽어도 앱은 멀쩡해 보이고 아무 흔적이 남지 않는다.
 * 그 구멍을 메우는 통로.
 *
 * 크래시는 Crashlytics 가 알아서 잡으므로 여기로 보낼 필요가 없다.
 */
interface ErrorReporter {

    /**
     * @param throwable 원인 예외.
     * @param context 그룹핑·재현에 필요한 값(엔드포인트, HTTP 코드 등). 개인정보는 넣지 않는다.
     */
    fun report(throwable: Throwable, context: Map<String, String> = emptyMap())

    /** 예외 없이 "이런 상태가 나왔다" 만 남길 때. */
    fun log(message: String)
}
