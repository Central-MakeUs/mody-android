package com.makeus.mody.feature.group

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.group.contract.GroupIntent
import com.makeus.mody.feature.group.contract.GroupState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 그룹 그래프 백스택에 scope 된 단일 ViewModel.
 * 참여/생성/초대 화면이 같은 인스턴스를 공유한다.
 *
 * NOTE: 현재는 UI-first 스캐폴드. 서버 연동(코드 검증/그룹 생성/초대코드 발급/
 *       클립보드/카카오 공유)은 TODO 로 표시. 백엔드 스펙 확정 후 연결.
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<GroupState, GroupIntent>(GroupState()) {

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
                if (currentState.isGroupNameValid) {
                    // TODO(group): 실제 서버 그룹 생성 API 로 교체.
                    //  응답 result.code (영문+숫자 6자리) 를 inviteCode 로 세팅.
                    val mockCode = generateMockInviteCode()
                    setState { copy(inviteCode = mockCode, codeCopied = false) }
                    navigationHelper.navigate(NavigationEvent.To(GroupGraph.GroupShareRoute))
                }

            is GroupIntent.CopyCodeClicked ->
                // 실제 클립보드 쓰기는 Screen(LocalClipboardManager)에서 처리. 여기선 상태만.
                setState { copy(codeCopied = true) }

            is GroupIntent.KakaoShareClicked -> {
                // TODO(group): 카카오톡 공유 SDK 연동
            }

            is GroupIntent.ShareDoneClicked -> {
                // TODO(group): 온보딩/그룹 완료 → 메인 그래프로 핸드오프
            }

            is GroupIntent.BackClicked ->
                navigationHelper.navigate(NavigationEvent.Up)
        }
    }

    private fun join() {
        // TODO(group): 서버에 joinCode 검증 요청.
        //  성공 → 메인 그래프, 실패 → setState { copy(joinError = ...) }
    }

    // TODO(group): 서버 응답 result.code 로 대체할 mock. 영문 대문자 + 숫자 6자리.
    private fun generateMockInviteCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars.random() }.joinToString("")
    }
}
