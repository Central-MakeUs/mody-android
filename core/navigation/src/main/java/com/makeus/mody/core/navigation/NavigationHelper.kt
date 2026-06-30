package com.makeus.mody.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationHelper @Inject constructor() {
    private val _navigationFlow = Channel<NavigationEvent>(BUFFERED)
    val navigationFlow = _navigationFlow.receiveAsFlow()

    /**
     * 네비게이션 이벤트 발행. 버퍼가 가득 차거나 채널이 닫히면 trySend 가 실패할 수 있으므로
     * 성공 여부를 반환해 호출부에서 드랍을 감지할 수 있게 한다.
     */
    fun navigate(event: NavigationEvent): Boolean =
        _navigationFlow.trySend(event).isSuccess
}

sealed class NavigationEvent {
    data class To(val route: Route, val popUpTo: Boolean = false) : NavigationEvent()
    data object Up : NavigationEvent()
    data class TopLevelTo(val route: Route) : NavigationEvent()
}
