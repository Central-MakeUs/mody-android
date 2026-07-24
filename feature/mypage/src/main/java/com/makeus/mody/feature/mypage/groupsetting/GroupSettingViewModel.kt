package com.makeus.mody.feature.mypage.groupsetting

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.groupsetting.contract.GroupSettingIntent
import com.makeus.mody.feature.mypage.groupsetting.contract.GroupSettingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupSettingViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<GroupSettingState, GroupSettingIntent>(GroupSettingState()) {

    init {
        load()
    }

    override suspend fun processIntent(intent: GroupSettingIntent) {
        when (intent) {
            is GroupSettingIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
            is GroupSettingIntent.LeaveClicked -> setState { copy(leaveTarget = intent.group) }
            is GroupSettingIntent.LeaveDismissed -> setState { copy(leaveTarget = null) }
            is GroupSettingIntent.LeaveConfirmed -> leaveGroup()
            is GroupSettingIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        try {
            val groups = groupRepository.getMyGroups()
            setState { copy(groups = groups, isLoading = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.toErrorAlert("그룹 목록을 불러오지 못했어요")) }
        }
    }

    private fun leaveGroup() = viewModelScope.launch {
        val target = currentState.leaveTarget ?: return@launch
        if (currentState.isProcessing) return@launch
        setState { copy(isProcessing = true) }
        try {
            groupRepository.leaveGroup(target.groupId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(leaveTarget = null, isProcessing = false, error = e.toErrorAlert("그룹 나가기에 실패했어요"))
            }
            return@launch
        }
        // 나가기는 이미 성공 — 재조회 실패가 "나가기 실패"로 보이면 사용자가 재시도하게 되므로
        // 우선 로컬에서 제거하고, 목록 재조회는 best-effort 로만 반영한다.
        setState {
            copy(
                groups = groups.filterNot { it.groupId == target.groupId },
                leaveTarget = null,
                isProcessing = false,
            )
        }
        runCatching { groupRepository.getMyGroups() }
            .onSuccess { refreshed -> setState { copy(groups = refreshed) } }
    }
}
