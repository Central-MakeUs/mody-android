package com.makeus.mody.feature.group.contract

import com.makeus.mody.core.commonui.base.UiState

/** 그룹 참여 코드 검증 실패 사유 */
enum class JoinCodeError { NOT_FOUND, FULL }

data class GroupState(
    // 코드로 그룹 참여
    val joinCode: String = "",
    val joinError: JoinCodeError? = null,
    // 그룹 생성 - 이름
    val groupName: String = "",
    // 그룹 생성 실패 토스트 메시지(1회성, 표시 후 null 로 소비)
    val createError: String? = null,
    // 친구 초대 - 내 그룹 초대 코드 (null = 로딩중)
    val inviteCode: String? = null,
    val codeCopied: Boolean = false,
    // 생성/참여 네트워크 진행중
    val isLoading: Boolean = false,
) : UiState {

    val isJoinEnabled: Boolean
        get() = joinCode.length == JOIN_CODE_LENGTH && joinError == null && !isLoading
    val isGroupNameValid: Boolean get() = groupName.isNotBlank() && groupName.length <= GROUP_NAME_MAX

    companion object {
        const val JOIN_CODE_LENGTH = 6
        const val GROUP_NAME_MAX = 14
        const val MAX_MEMBERS = 12
    }
}
