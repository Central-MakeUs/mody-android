package com.makeus.mody.feature.feed.feed.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class FeedIntent : UiIntent {
    /** 날짜 셀렉터 탭 */
    data object DateClicked : FeedIntent()

    /** 그룹 멤버 아이콘 탭 */
    data object MembersClicked : FeedIntent()

    /** 알림 아이콘 탭 */
    data object AlarmClicked : FeedIntent()

    /** 엠티 스테이트 "콕 찌르기 하러 가기" 버튼 탭 */
    data object PokeClicked : FeedIntent()

    /** FAB(피드 작성) 탭 */
    data object WriteClicked : FeedIntent()
}
