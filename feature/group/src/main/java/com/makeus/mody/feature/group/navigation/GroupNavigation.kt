package com.makeus.mody.feature.group.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.GroupGraphBaseRoute

fun NavGraphBuilder.groupNavGraph() {
    navigation<GroupGraphBaseRoute>(startDestination = GroupGraph.GroupEntryRoute) {
        composable<GroupGraph.GroupEntryRoute> { GroupEntryScreen() }
        composable<GroupGraph.JoinGroupRoute> { JoinGroupScreen() }
        composable<GroupGraph.CreateGroupRoute> { CreateGroupScreen() }
        composable<GroupGraph.GroupShareRoute> { GroupShareScreen() }
    }
}

@Composable
private fun GroupEntryScreen() {
    Text(text = "GroupEntryScreen")
}

@Composable
private fun JoinGroupScreen() {
    Text(text = "JoinGroupScreen")
}

@Composable
private fun CreateGroupScreen() {
    Text(text = "CreateGroupScreen")
}

@Composable
private fun GroupShareScreen() {
    Text(text = "GroupShareScreen")
}
