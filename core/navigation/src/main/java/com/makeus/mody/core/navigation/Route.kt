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
    @Serializable
    data object HeightWeightInputRoute : OnboardingGraph

    @Serializable
    data object MealAlarmTimeRoute : OnboardingGraph

    @Serializable
    data object ExerciseAlarmTimeRoute : OnboardingGraph

    @Serializable
    data object OnboardingCompleteRoute : OnboardingGraph
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
