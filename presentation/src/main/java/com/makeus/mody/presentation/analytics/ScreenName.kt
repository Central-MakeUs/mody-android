package com.makeus.mody.presentation.analytics

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.makeus.mody.core.domain.analytics.AnalyticsScreenParams
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.feature.group.analytics.toEntryPoint

/**
 * 하단 탭이 붙는 컨테이너 목적지. 이 화면 자체는 로깅하지 않는다 —
 * 실제로 보이는 건 탭(피드/챌린지/마이)이라 [MainScreenViewModel] 이 탭 이름으로 남긴다.
 */
internal const val MainScreenName = "Main"

/**
 * type-safe Navigation 의 라우트 문자열을 GA4 화면 이름으로 줄인다.
 *
 * 라우트는 `com.makeus.mody.core.navigation.ChallengeGraph.WeeklyChallengeDetailRoute/{groupId}/...`
 * 처럼 패키지 전체 + 인자 자리표시자가 붙어 온다. 자리표시자라 실제 값(제목 등)이 섞이지 않아
 * 개인정보가 새지 않는다.
 *
 * @return `WeeklyChallengeDetail`. 알아볼 수 없으면 null(로깅 생략).
 */
internal fun String?.toScreenName(): String? {
    val pattern = this?.substringBefore('/')?.substringBefore('?') ?: return null
    val simpleName = pattern.substringAfterLast('.').takeIf { it.isNotBlank() } ?: return null
    // 라우트 네이밍이 `XxxRoute` 로 통일돼 있어 접미사는 노이즈다. 다 떼면 빈 이름이 되는
    // `Route` 같은 경우만 원본을 유지한다.
    return simpleName.removeSuffix("Route").takeIf { it.isNotBlank() } ?: simpleName
}

/**
 * 화면 노출에 붙일 파라미터. 라우트 인자를 통째로 보내지 않는다 — groupId·챌린지 제목처럼
 * 식별자나 끝없이 늘어나는 값이 섞이므로, 명세에 있는 화면·값만 골라 꺼낸다.
 */
internal fun NavBackStackEntry.screenParams(): Map<String, String> = when {
    destination.hasRoute<GroupGraph.GroupEntryRoute>() ->
        AnalyticsScreenParams.groupEntry(toRoute<GroupGraph.GroupEntryRoute>().source.toEntryPoint())
    else -> emptyMap()
}
