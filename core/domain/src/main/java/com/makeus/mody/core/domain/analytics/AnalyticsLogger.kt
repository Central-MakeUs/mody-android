package com.makeus.mody.core.domain.analytics

/**
 * 사용 로그(GA4). 구현은 Firebase Analytics.
 *
 * 개인정보는 넣지 않는다 — 닉네임·이메일·사진 URL 처럼 사람을 특정할 수 있는 값은 금지고,
 * 화면 이름·버튼 종류·성공 여부 같은 것만 남긴다.
 */
interface AnalyticsLogger {

    /**
     * 화면 노출. Compose 는 자동 수집이 Activity 단위라 이 앱은 전 화면이 MainActivity 하나로
     * 뭉친다 — 화면 구분은 직접 남겨야 한다.
     */
    fun logScreenView(screenName: String)

    /** 커스텀 이벤트. GA4 규칙상 이름·파라미터 키는 snake_case. */
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}
