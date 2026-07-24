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
            // 나간 결과 반영을 위해 목록 재조회
            val groups = groupRepository.getMyGroups()
            setState { copy(groups = groups, leaveTarget = null, isProcessing = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(leaveTarget = null, isProcessing = false, error = e.toErrorAlert("그룹 나가기에 실패했어요"))
            }
        }
    }
}
