package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable data object AuthGraphRoute : Route

sealed interface AuthGraph : Route {
    @Serializable data object LoginRoute : AuthGraph
    @Serializable data object BasicInfoRoute : AuthGraph
    @Serializable data object GroupRoute : AuthGraph
}

@Serializable data object OnboardingGraphRoute : Route

sealed interface OnboardingGraph : Route {
    @Serializable data object NicknameRoute : OnboardingGraph
    @Serializable data object BirthRoute : OnboardingGraph
    @Serializable data object WeightRoute : OnboardingGraph
    @Serializable data object AlarmRoute : OnboardingGraph
}
