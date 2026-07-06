package com.makeus.mody.feature.group.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class GroupIntent : UiIntent {
    // 코드로 그룹 참여
    data class JoinCodeChanged(val value: String) : GroupIntent()
    data object JoinClicked : GroupIntent()

    // 새로운 그룹 만들기 진입
    data object CreateGroupClicked : GroupIntent()

    // 그룹 이름 입력 → 다음
    data class GroupNameChanged(val value: String) : GroupIntent()
    data object GroupNameNext : GroupIntent()

    // 친구 초대 (카카오 공유는 Context 필요 → Screen 에서 직접 처리)
    data object CopyCodeClicked : GroupIntent()
    data object ShareDoneClicked : GroupIntent()

    // 뒤로가기
    data object BackClicked : GroupIntent()
}
