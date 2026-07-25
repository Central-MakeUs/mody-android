package com.makeus.mody.core.domain.notification

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 그룹홈 알림 딥링크(/groups/{id}/home)로 진입할 때 이동할 groupId 를 임시 보관.
 * 그룹홈은 별도 라우트가 없고 Feed 탭 + 활성 그룹 전환으로 구현되므로,
 * MainActivity 가 set → MainScreenViewModel 이 Feed 탭으로 전환 + FeedViewModel 이 해당 그룹 선택.
 *
 * StateFlow 로 노출해 여러 소비자(탭 전환/그룹 선택)가 동시에 반응할 수 있게 하되,
 * consume() 으로 실제 선택이 끝나면 비운다(1회성). process 생존 동안만 유효, 영속 아님.
 */
@Singleton
class PendingGroupSelectionHolder @Inject constructor() {
    private val _pendingGroupId = MutableStateFlow<Long?>(null)
    val pendingGroupId: StateFlow<Long?> = _pendingGroupId.asStateFlow()

    fun set(groupId: Long) {
        _pendingGroupId.value = groupId
    }

    /** 현재 대기 중인 groupId 를 반환하고 비운다(1회성). 없으면 null. */
    fun consume(): Long? = _pendingGroupId.value?.also { _pendingGroupId.value = null }
}
