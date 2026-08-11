package com.makeus.mody.feature.mypage.groupsetting

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.navigation.GroupEntrySource
import com.makeus.mody.core.navigation.GroupGraph
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
        if (currentState.groups.isEmpty()) {
            redirectToGroupOnboarding()
            return@launch
        }
        runCatching { groupRepository.getMyGroups() }
            .onSuccess { refreshed ->
                if (refreshed.isEmpty()) redirectToGroupOnboarding()
                else setState { copy(groups = refreshed) }
            }
    }

    /**
     * 그룹이 하나도 없으면 그룹 참여/생성 화면으로 강제 이동(백스택 제거 → 피드 복귀 불가).
     * 세션 플래그도 내려 재접속 시 시작 라우팅이 GROUP 으로 가게 한다.
     */
    private suspend fun redirectToGroupOnboarding() {
        // 세션 반영이 실패해도 이동은 막지 않는다 — 여기서 멈추면 그룹이 하나도 없는
        // 설정 화면에 갇힌다. 세션은 다음 실행에 같은 검사가 돌며 스스로 복구된다.
        // 대신 조용히 삼키지 않는다: 반복되면 세션 저장이 깨졌다는 신호다.
        try {
            groupRepository.markNoGroups()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "그룹 없음 세션 반영 실패 — 다음 실행에서 다시 시도된다", e)
        }
        navigationHelper.navigate(NavigationEvent.To(
            GroupGraph.GroupEntryRoute(source = GroupEntrySource.NoGroup),
            popUpTo = true,
        ))
    }

    private companion object {
        const val TAG = "GroupSettingViewModel"
    }
}
