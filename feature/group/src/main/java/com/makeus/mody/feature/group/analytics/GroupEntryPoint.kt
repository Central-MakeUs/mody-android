package com.makeus.mody.feature.group.analytics

import com.makeus.mody.core.domain.analytics.GroupEntryPoint
import com.makeus.mody.core.navigation.GroupEntrySource

/**
 * 라우트의 진입 출처 → 사용 로그 entry_source 값.
 * 그룹 화면 노출(MainActivity)과 참여·생성 이벤트(GroupViewModel)가 같은 값을 쓰도록 여기 한 곳에 둔다.
 */
fun GroupEntrySource.toEntryPoint(): GroupEntryPoint = when (this) {
    GroupEntrySource.Onboarding -> GroupEntryPoint.SIGNUP
    GroupEntrySource.Feed -> GroupEntryPoint.FEED
    GroupEntrySource.NoGroup -> GroupEntryPoint.REJOIN
}
