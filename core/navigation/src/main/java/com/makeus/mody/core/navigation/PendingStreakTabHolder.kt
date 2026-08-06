package com.makeus.mody.core.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 피드 엠티 스테이트의 "콕 찌르기 하러 가기" → 챌린지 탭 연속 기록 이동 요청을 임시 보관.
 *
 * 챌린지 탭은 라우트가 아니라 하단 네비게이션 탭이고, 연속 기록은 그 안의 서브탭이라
 * [NavigationEvent] 로 표현할 수 없다. 그래서 FeedViewModel 이 set → MainScreenViewModel 이
 * 챌린지 탭으로 전환 → ChallengeViewModel 이 화면 진입 시 consume 해 연속 기록 서브탭으로 맞춘다.
 *
 * consume 은 ChallengeViewModel 만 한다. 두 소비자가 같은 StateFlow 를 collect 하면
 * 한쪽이 먼저 비워 다른 쪽이 conflate 로 값을 놓칠 수 있어, 챌린지 쪽은 흐름 구독이 아니라
 * 화면 진입 시점에 값을 당겨간다(탭 전환보다 확실히 뒤). process 생존 동안만 유효, 영속 아님.
 */
@Singleton
class PendingStreakTabHolder @Inject constructor() {
    private val _pending = MutableStateFlow(false)
    val pending: StateFlow<Boolean> = _pending.asStateFlow()

    fun set() {
        _pending.value = true
    }

    /** 대기 중인 요청이 있었는지 반환하고 비운다(1회성). */
    fun consume(): Boolean = _pending.value.also { _pending.value = false }
}
