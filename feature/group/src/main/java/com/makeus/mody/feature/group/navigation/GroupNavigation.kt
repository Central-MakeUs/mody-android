package com.makeus.mody.feature.group.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.makeus.mody.core.navigation.GroupGraph
import com.makeus.mody.core.navigation.GroupGraphBaseRoute
import com.makeus.mody.feature.group.GroupViewModel
import com.makeus.mody.feature.group.create.CreateGroupScreen
import com.makeus.mody.feature.group.entry.GroupEntryScreen
import com.makeus.mody.feature.group.share.GroupShareScreen

fun NavGraphBuilder.groupNavGraph(navController: NavHostController) {
    navigation<GroupGraphBaseRoute>(startDestination = GroupGraph.GroupEntryRoute()) {
        composable<GroupGraph.GroupEntryRoute> { entry ->
            val route = entry.toRoute<GroupGraph.GroupEntryRoute>()
            GroupEntryScreen(
                viewModel = entry.sharedViewModel(navController),
                source = route.source,
            )
        }
        composable<GroupGraph.CreateGroupRoute> { entry ->
            CreateGroupScreen(entry.sharedViewModel(navController))
        }
        composable<GroupGraph.GroupShareRoute> { entry ->
            GroupShareScreen(entry.sharedViewModel(navController))
        }
        // TODO(group): 코드 참여 성공 후 진입할 그룹 상세/대기 화면. 서버 연동 시 구현.
        composable<GroupGraph.JoinGroupRoute> {
            Text(text = "JoinGroupScreen")
        }
    }
}

/**
 * 그룹 그래프 백스택 엔트리에 scope 된 단일 ViewModel.
 * 참여/생성/초대 화면이 같은 인스턴스를 공유한다.
 */
@Composable
private fun NavBackStackEntry.sharedViewModel(
    navController: NavHostController,
): GroupViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(GroupGraphBaseRoute)
    }
    return hiltViewModel(parentEntry)
}
