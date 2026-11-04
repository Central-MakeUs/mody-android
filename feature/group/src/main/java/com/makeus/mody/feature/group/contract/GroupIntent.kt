package com.makeus.mody.feature.group.contract

import com.makeus.mody.core.commonui.base.UiIntent
import com.makeus.mody.core.navigation.GroupEntrySource

sealed class GroupIntent : UiIntent {
    // 그룹 그래프 화면 진입. 공유 ViewModel 이라 진입 출처를 라우트에서 넘겨받는다.
    data class Entered(val source: GroupEntrySource) : GroupIntent()

    // 코드로 그룹 참여
    data class JoinCodeChanged(val value: String) : GroupIntent()
    data object JoinClicked : GroupIntent()

    // 새로운 그룹 만들기 진입
    data object CreateGroupClicked : GroupIntent()

    // 그룹 이름 입력 → 다음
    data class GroupNameChanged(val value: String) : GroupIntent()
    data object GroupNameNext : GroupIntent()

    // 그룹 생성 실패 토스트 표시 완료 → 상태 소비
    data object CreateErrorShown : GroupIntent()

    // 친구 초대 (카카오 공유 실행은 Context 필요 → Screen 에서 직접 처리, 여기선 로그만)
    data object CopyCodeClicked : GroupIntent()
    data object KakaoShareClicked : GroupIntent()
    data object ShareDoneClicked : GroupIntent()

    // 뒤로가기
    data object BackClicked : GroupIntent()
}
