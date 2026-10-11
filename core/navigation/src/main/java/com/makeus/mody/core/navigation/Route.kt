package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

/**
 * 모든 화면 목적지의 공통 타입. NavigationEvent 가 이 타입으로 목적지를 받는다.
 *
 * 각 feature 의 Route 는 그 feature 의 :api 모듈에 있다(예: `:feature:feed:api` 의 FeedGraph).
 * 모듈을 넘어 구현해야 해서 sealed 가 아니다.
 */
interface Route

/** 하단 탭을 품는 앱 셸. 어느 feature 에도 속하지 않아 여기 둔다. */
@Serializable
data object MainRoute : Route
