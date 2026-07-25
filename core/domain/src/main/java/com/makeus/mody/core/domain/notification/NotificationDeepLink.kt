package com.makeus.mody.core.domain.notification

/**
 * 푸시 알림 data payload 에서 뽑은 이동 정보.
 * @param link 서버가 보내는 이동 경로(URL path). 예: "/records/meal/new", "/groups/12/home".
 *  화면 매핑은 이 경로를 파싱해서 결정한다(NotificationLinkParser).
 */
data class NotificationDeepLink(
    val link: String,
) {
    companion object {
        // FCM data payload 키(서버 계약) + 알림 탭 인텐트 extra 키. 서비스↔MainActivity 공유.
        const val KEY_LINK = "link"
    }
}
