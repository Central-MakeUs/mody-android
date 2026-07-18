package com.makeus.mody.core.domain.notification

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 탭으로 실행됐을 때의 이동 정보를 임시 보관.
 * MainActivity 가 인텐트 extra 에서 뽑아 set, NavHost 준비된 뒤 consume(1회성)해 라우팅한다.
 * (InviteCodeHolder 와 동일 패턴 — process 생존 동안만 유효, 영속 아님)
 */
@Singleton
class NotificationDeepLinkHolder @Inject constructor() {
    @Volatile
    private var pending: NotificationDeepLink? = null

    fun set(deepLink: NotificationDeepLink) {
        pending = deepLink
    }

    /** 보관된 딥링크를 반환하고 비운다(1회성). 없으면 null. */
    fun consume(): NotificationDeepLink? = pending?.also { pending = null }
}
