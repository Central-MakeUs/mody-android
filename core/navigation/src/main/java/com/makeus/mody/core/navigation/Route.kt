package com.makeus.mody.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable data object AuthGraphRoute : Route

sealed interface AuthGraph : Route {
    @Serializable data object LoginRoute : AuthGraph
    @Serializable data object BasicInfoRoute : AuthGraph
    @Serializable data object GroupRoute : AuthGraph
}
