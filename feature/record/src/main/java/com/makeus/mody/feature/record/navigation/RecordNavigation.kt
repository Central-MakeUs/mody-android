package com.makeus.mody.feature.record.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.makeus.mody.core.navigation.RecordGraph
import com.makeus.mody.core.navigation.RecordGraphBaseRoute
import com.makeus.mody.feature.record.food.RecordFoodScreen
import com.makeus.mody.feature.record.health.RecordHealthScreen

fun NavGraphBuilder.recordNavGraph() {
    navigation<RecordGraphBaseRoute>(startDestination = RecordGraph.FoodRoute) {
        composable<RecordGraph.FoodRoute> { RecordFoodScreen() }
        composable<RecordGraph.HealthRoute> { RecordHealthScreen() }
    }
}
