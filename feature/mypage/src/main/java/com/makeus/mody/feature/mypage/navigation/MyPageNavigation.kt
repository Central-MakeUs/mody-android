package com.makeus.mody.feature.mypage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.makeus.mody.core.navigation.MyPageGraph
import com.makeus.mody.core.navigation.MyPageGraphBaseRoute
import com.makeus.mody.feature.mypage.groupsetting.GroupSettingScreen
import com.makeus.mody.feature.mypage.profile.ProfileEditScreen

fun NavGraphBuilder.myPageNavGraph() {
    navigation<MyPageGraphBaseRoute>(startDestination = MyPageGraph.ProfileEditRoute) {
        composable<MyPageGraph.ProfileEditRoute> { ProfileEditScreen() }
        composable<MyPageGraph.GroupSettingRoute> { GroupSettingScreen() }
    }
}
