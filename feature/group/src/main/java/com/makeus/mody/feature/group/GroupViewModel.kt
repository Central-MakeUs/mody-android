package com.makeus.mody.feature.group

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.group.contract.GroupIntent
import com.makeus.mody.feature.group.contract.GroupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * 그룹 그래프 백스택에 scope 된 단일 ViewModel.
 * 참여/생성/초대 화면이 같은 인스턴스를 공유한다.
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
    private val groupRepository: GroupRepository,
    inviteCodeHolder: InviteCodeHolder,
) : BaseViewModel<GroupState, GroupIntent>(GroupState()) {

    init {
        // 딥링크로 들어온 초대 코드가 있으면 참여 코드 필드에 자동 입력(1회성).
        inviteCodeHolder.consume()?.let { code ->
            setState { copy(joinCode = code.uppercase()) }
        }
    }

    override suspend fun processIntent(intent: GroupIntent) {
        when (intent) {
            is GroupIntent.JoinCodeChanged ->
                // 코드는 영문 대문자 기준 → 입력 소스(붙여넣기/하드웨어) 무관하게 정규화
                setState { copy(joinCode = intent.value.uppercase(), joinError = null) }

            is GroupIntent.JoinClicked -> join()

            is GroupIntent.CreateGroupClicked ->
                navigationHelper.navigate(NavigationEvent.To(GroupGraph.CreateGroupRoute))

            is GroupIntent.GroupNameChanged ->
                setState { copy(groupName = intent.value) }

            is GroupIntent.GroupNameNext ->
                if (currentState.isGroupNameValid) createGroup()

            is GroupIntent.CreateErrorShown ->
                setState { copy(createError = null) }

            is GroupIntent.CopyCodeClicked ->
                // 실제 클립보드 쓰기는 Screen(LocalClipboardManager)에서 처리. 여기선 상태만.
                setState { copy(codeCopied = true) }

            is GroupIntent.ShareDoneClicked ->
                // 그룹 생성 완료 → 메인으로. 온보딩/그룹 백스택 제거.
                navigationHelper.navigate(NavigationEvent.To(MainRoute, popUpTo = true))

            is GroupIntent.BackClicked ->
                // 생성/참여 요청 중에는 화면을 떠나지 않는다. ViewModel 이 그룹 그래프
                // 백스택 엔트리에 scope 되어 있어(GroupNavigation.sharedViewModel) 화면을
                // 벗어나도 살아남고, 진행 중이던 코루틴이 뒤늦게 성공하면 사용자가 이미
                // 떠난 뒤에 GroupShareRoute 로 끌고 간다.
                if (!currentState.isLoading) navigationHelper.navigate(NavigationEvent.Up)
        }
    }

    private suspend fun createGroup() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true) }
        try {
            val group = groupRepository.createGroup(currentState.groupName)
            setState { copy(isLoading = false, inviteCode = group.code, codeCopied = false) }
            navigationHelper.navigate(NavigationEvent.To(GroupGraph.GroupShareRoute))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 서버/네트워크/기타 분기는 toErrorAlert 공통 규칙을 따른다.
            setState { copy(isLoading = false, createError = e.toErrorAlert("그룹 생성에 실패했어요")) }
        }
    }

    private suspend fun join() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true, joinError = null) }
        try {
            groupRepository.joinGroup(currentState.joinCode)
            navigationHelper.navigate(NavigationEvent.To(MainRoute, popUpTo = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 서버 에러코드(GROUP3xx)별 기획 문구 분기. 미분기 코드는 서버 message 노출,
            // 네트워크 등 비 HTTP 예외는 기술 메시지가 새지 않게 폴백 문구.
            val http = e as? HttpResponseException
            val message = when (http?.errorCode) {
                ModyErrorCode.GROUP302 -> "존재하지 않는 코드입니다."
                ModyErrorCode.GROUP304 -> "참여할 수 있는 그룹 개수를 초과했어요."
                ModyErrorCode.GROUP305 -> "이미 참여 중인 그룹이에요."
                ModyErrorCode.GROUP307 -> "그룹 인원이 가득 찼어요."
                null -> "그룹 참여에 실패했어요."
                else -> http.msg ?: "그룹 참여에 실패했어요."
            }
            setState { copy(isLoading = false, joinError = message) }
        }
    }
}
