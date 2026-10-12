package com.makeus.mody.feature.auth.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object AuthGraphBaseRoute : Route

sealed interface AuthGraph : Route {
    @Serializable
    data object LoginRoute : AuthGraph
}
