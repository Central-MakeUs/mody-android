package com.makeus.mody.core.domain.feature

/**
 * 응원 댓글 기능 노출 여부.
 *
 * 댓글에는 신고 경로가 없어 스토어 심사에서 리젝될 소지가 있어 닫아둔다.
 *
 * ## 왜 Remote Config 가 아닌가
 *
 * 원격 플래그(comment_flag)로 뒀다가 코드로 옮겼다. 콘솔에 파라미터를 만들기 전까지는
 * 기본값 false 가 그대로 나가는데, 그 상태에선 "원격으로 켤 수 있다"는 기대만 남고 실제로
 * 켤 수단이 없다. 켤 계획이 정해지면(=댓글 신고 기능이 붙으면) 그때 다시 원격으로 빼는 게
 * 낫다 — 지금은 배포 없이 켜야 할 이유가 없다.
 *
 * ## 켤 때
 *
 * [ENABLED] 만 true 로 바꿔 배포한다. 이 하나로 아래가 전부 함께 돌아온다:
 *
 *  - 피드 카드의 상세 진입 화살표와 탭 (FeedState.commentEnabled)
 *  - 상세 화면의 댓글 목록·입력바·전송 (RecordDetailState.commentEnabled)
 *  - 알림함의 댓글 알림 항목 (NotificationViewModel.filterByFeatureFlags)
 *  - 알림 설정의 "응원 댓글 알림" 토글 행 (NotificationSettingState.commentFeatureEnabled)
 *
 * 값을 여기 한 곳에만 두는 이유가 이거다. 화면마다 false 를 박아두면 켤 때 하나를
 * 빠뜨려서, 들어갈 수 없는데 알림만 오거나 진입은 되는데 빈 화면이 뜬다.
 */
object CommentFeature {
    const val ENABLED = false
}
