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
     *
     * @param params 화면에 붙는 성격 값(그룹 참여 화면의 entry_source 등). 식별자는 넣지 않는다.
     */
    fun logScreenView(screenName: String, params: Map<String, String> = emptyMap())

    /** 커스텀 이벤트. GA4 규칙상 이름·파라미터 키는 snake_case. 명세 이벤트는 [log] 로 보낸다. */
    fun logEvent(name: String, params: Map<String, String> = emptyMap())

    /** 사용자 속성. null 이면 지운다. 이후 이벤트에 자동으로 붙는다. */
    fun setUserProperty(name: String, value: String?)
}

/** 명세 이벤트 전송. 이름·파라미터를 호출부에서 조립하지 않도록 [AnalyticsEvent] 로만 받는다. */
fun AnalyticsLogger.log(event: AnalyticsEvent) = logEvent(event.name, event.params)
