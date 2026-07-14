package com.makeus.mody.core.domain.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션 완전 만료(재발급·무음 재로그인 모두 실패) 이벤트 버스.
 * 네트워크 계층에서 발행하고, 최상위(presentation)에서 구독해 로그인 화면으로 보낸다.
 */
@Singleton
class SessionExpiredNotifier @Inject constructor() {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(Unit)
    }
}
