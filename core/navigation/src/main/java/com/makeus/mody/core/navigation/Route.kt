package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
data object AuthGraphBaseRoute : Route

sealed interface AuthGraph : Route {
    @Serializable
    data object LoginRoute : AuthGraph
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
data object RecordGraphBaseRoute : Route

sealed interface RecordGraph : Route {
    /** 식사 기록 (record/food) */
    @Serializable
    data object FoodRoute : RecordGraph

    /** 운동 기록 (record/health) */
    @Serializable
    data object HealthRoute : RecordGraph
}

@Serializable
data object NotificationGraphBaseRoute : Route

sealed interface NotificationGraph : Route {
    /** 알림 목록 */
    @Serializable
    data object NotificationRoute : NotificationGraph
}

@Serializable
data object MainRoute : Route
