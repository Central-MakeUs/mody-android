package com.makeus.mody.feature.group

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.analytics.AnalyticsEvent
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import com.makeus.mody.core.domain.analytics.GroupJoinFailReason
import com.makeus.mody.core.domain.analytics.ShareMethod
import com.makeus.mody.core.domain.analytics.log
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.model.error.HttpResponseException
import com.makeus.mody.core.model.error.ModyErrorCode
import com.makeus.mody.core.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.navigation.GroupEntrySource
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.group.analytics.toEntryPoint
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
    private val analyticsLogger: AnalyticsLogger,
) : BaseViewModel<GroupState, GroupIntent>(GroupState()) {

    /** 그룹 그래프에 들어온 출처. 화면 진입 때 [GroupIntent.Entered] 로 채운다. 로그 전용. */
    private var entrySource = GroupEntrySource.Onboarding

    init {
        // 딥링크로 들어온 초대 코드가 있으면 참여 코드 필드에 자동 입력(1회성).
        inviteCodeHolder.consume()?.let { code ->
            setState { copy(joinCode = code.uppercase()) }
        }
    }

    override suspend fun processIntent(intent: GroupIntent) {
        when (intent) {
            is GroupIntent.Entered -> entrySource = intent.source

            is GroupIntent.JoinCodeChanged ->
                // 코드는 영문 대문자 기준 → 입력 소스(붙여넣기/하드웨어) 무관하게 정규화
                setState { copy(joinCode = intent.value.uppercase(), joinError = null) }

            is GroupIntent.JoinClicked -> join()

            is GroupIntent.CreateGroupClicked -> {
                analyticsLogger.log(AnalyticsEvent.GroupCreateClick(entrySource.toEntryPoint()))
                navigationHelper.navigate(NavigationEvent.To(GroupGraph.CreateGroupRoute(source = entrySource)))
            }

            is GroupIntent.GroupNameChanged ->
                setState { copy(groupName = intent.value) }

            is GroupIntent.GroupNameNext ->
                if (currentState.isGroupNameValid) createGroup()

            is GroupIntent.CreateErrorShown ->
                setState { copy(createError = null) }

            is GroupIntent.CopyCodeClicked -> {
                // 실제 클립보드 쓰기는 Screen(LocalClipboardManager)에서 처리. 여기선 상태만.
                analyticsLogger.log(AnalyticsEvent.GroupShareClick(ShareMethod.COPY))
                setState { copy(codeCopied = true) }
            }

            is GroupIntent.KakaoShareClicked ->
                analyticsLogger.log(AnalyticsEvent.GroupShareClick(ShareMethod.KAKAO))

            is GroupIntent.ShareDoneClicked -> {
                // 그룹 생성 완료 → 메인으로. 온보딩/그룹 백스택 제거.
                analyticsLogger.log(AnalyticsEvent.GroupCreateFinish)
                navigationHelper.navigate(NavigationEvent.To(MainRoute, popUpTo = true))
            }

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
            analyticsLogger.log(AnalyticsEvent.GroupCreateSuccess(entrySource.toEntryPoint()))
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
            analyticsLogger.log(AnalyticsEvent.JoinGroup(entrySource.toEntryPoint()))
            navigationHelper.navigate(NavigationEvent.To(MainRoute, popUpTo = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 서버 에러코드(GROUP3xx)별 기획 문구 분기. 미분기 코드는 서버 message 노출,
            // 네트워크 등 비 HTTP 예외는 기술 메시지가 새지 않게 폴백 문구.
            val http = e as? HttpResponseException
            analyticsLogger.log(AnalyticsEvent.GroupJoinFail(http.toJoinFailReason(), entrySource.toEntryPoint()))
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

/** 서버 에러 코드 → 사용 로그 fail_reason. 화면 문구 분기(join)와 같은 코드 기준. */
private fun HttpResponseException?.toJoinFailReason(): GroupJoinFailReason = when {
    this == null -> GroupJoinFailReason.NETWORK
    errorCode == ModyErrorCode.GROUP302 -> GroupJoinFailReason.INVALID_CODE
    errorCode == ModyErrorCode.GROUP304 -> GroupJoinFailReason.MAX_GROUP
    errorCode == ModyErrorCode.GROUP305 -> GroupJoinFailReason.ALREADY_JOINED
    errorCode == ModyErrorCode.GROUP307 -> GroupJoinFailReason.GROUP_FULL
    else -> GroupJoinFailReason.SERVER_ERROR
}
