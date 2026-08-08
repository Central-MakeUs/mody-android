package com.makeus.mody.core.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션 완전 만료(재발급·무음 재로그인 모두 실패) 상태.
 * 네트워크 계층에서 올리고, 최상위(presentation)에서 구독해 로그인 화면으로 보낸다.
 *
 * 일회성 이벤트(SharedFlow)가 아니라 상태로 들고 있는다 — 만료는 앱이 백그라운드에 있는
 * 동안에도 일어나는데, 그때 구독자가 없으면 이벤트는 그대로 사라지고 사용자는 죽은 세션으로
 * 로그인된 화면에 남는다. 상태로 두면 구독이 늦게 붙어도 받는다.
 */
@Singleton
class SessionExpiredNotifier @Inject constructor() {

    private val _expired = MutableStateFlow(false)

    /** true 면 세션이 죽었다. 로그인 화면으로 보낸 뒤 [consume] 으로 내린다. */
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    /** 여러 요청이 동시에 401 을 맞아도 같은 상태로 수렴한다(중복 통지 무해). */
    fun notifySessionExpired() {
        _expired.value = true
    }

    /** 처리 완료. 내려두지 않으면 재로그인 후에도 만료로 남아 곧장 로그인으로 튕긴다. */
    fun consume() {
        _expired.value = false
    }
}
