package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
data object AuthGraphBaseRoute : Route

sealed interface AuthGraph : Route {
    @Serializable
    data object LoginRoute : AuthGraph

    @Serializable
    data object SignUpRoute : AuthGraph
}

@Serializable
data object OnboardingGraphBaseRoute : Route

sealed interface OnboardingGraph : Route {
    // 입력 스텝 순서: 닉네임 → 생년월일 → 체중 → 알림 → 완료
    @Serializable
    data object NicknameRoute : OnboardingGraph

    @Serializable
    data object BirthRoute : OnboardingGraph

    @Serializable
    data object WeightRoute : OnboardingGraph

    @Serializable
    data object AlarmRoute : OnboardingGraph
}

@Serializable
data object GroupGraphBaseRoute : Route

sealed interface GroupGraph : Route {
    @Serializable
    data object GroupEntryRoute : GroupGraph

    @Serializable
    data object JoinGroupRoute : GroupGraph

    @Serializable
    data object CreateGroupRoute : GroupGraph

    @Serializable
    data object GroupShareRoute : GroupGraph
}

@Serializable
data object MainRoute : Route
