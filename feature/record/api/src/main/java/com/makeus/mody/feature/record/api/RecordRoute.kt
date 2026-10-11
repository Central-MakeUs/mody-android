package com.makeus.mody.feature.record.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

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
