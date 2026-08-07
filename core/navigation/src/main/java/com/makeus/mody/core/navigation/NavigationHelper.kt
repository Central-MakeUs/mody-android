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

    /**
     * 백스택에서 [route] 까지 pop (route 는 유지). 진입 경로와 무관하게 특정 화면으로 복귀할 때
     * 사용 — 예: 기록 작성 완료 → 어디서 진입했든(피드 FAB/알림 목록/FCM 푸시) 피드로.
     * [route] 가 스택에 없으면 Up 으로 폴백.
     */
    data class BackTo(val route: Route) : NavigationEvent()

    /**
     * 현재 화면을 백스택에서 빼고 [route] 로 이동(치환). 목적지에서 뒤로가기를 하면
     * 현재 화면이 아니라 그 이전 화면으로 돌아간다 — 예: 알림 목록에서 항목을 누르면
     * 목록은 떠나고 대상 화면만 남는다.
     */
    data class Replace(val route: Route) : NavigationEvent()
}
