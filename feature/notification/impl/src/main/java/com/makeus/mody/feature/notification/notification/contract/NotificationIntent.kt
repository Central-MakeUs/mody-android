package com.makeus.mody.feature.notification.notification.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class NotificationIntent : UiIntent {
    data object BackClicked : NotificationIntent()

    /** 목록 끝에 근접 시 다음 페이지 요청(무한 스크롤). */
    data object LoadMore : NotificationIntent()

    /** 알림 아이템 탭 → link 파싱해 해당 화면으로 이동. */
    data class ItemClicked(val link: String?) : NotificationIntent()
}
