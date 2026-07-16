package com.makeus.mody.core.domain.notification

/**
 * 푸시 알림 data payload 에서 뽑은 이동 정보.
 * @param type 서버가 보내는 알림 종류 문자열(예: "COMMENT", "CHALLENGE"). 화면 매핑 키.
 * @param targetId 상세로 갈 대상 id(있으면). 예: 댓글 알림의 recordId.
 */
data class NotificationDeepLink(
    val type: String,
    val targetId: Long? = null,
) {
    companion object {
        // FCM data payload 키(서버 계약) + 알림 탭 인텐트 extra 키. 서비스↔MainActivity 공유.
        const val KEY_TYPE = "type"
        const val KEY_TARGET_ID = "targetId"
    }
}
