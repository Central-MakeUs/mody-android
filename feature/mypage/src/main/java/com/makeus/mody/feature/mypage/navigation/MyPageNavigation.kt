package com.makeus.mody.feature.mypage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.makeus.mody.core.navigation.MyPageGraph
import com.makeus.mody.core.navigation.MyPageGraphBaseRoute
import com.makeus.mody.feature.mypage.groupsetting.GroupSettingScreen
import com.makeus.mody.feature.mypage.healthguide.HealthGuideScreen
import com.makeus.mody.feature.mypage.notification.NotificationSettingScreen
import com.makeus.mody.feature.mypage.profile.ProfileEditScreen
import com.makeus.mody.feature.mypage.support.SupportScreen

fun NavGraphBuilder.myPageNavGraph() {
    navigation<MyPageGraphBaseRoute>(startDestination = MyPageGraph.ProfileEditRoute) {
        composable<MyPageGraph.ProfileEditRoute> { ProfileEditScreen() }
        composable<MyPageGraph.NotificationSettingRoute> { NotificationSettingScreen() }
        composable<MyPageGraph.GroupSettingRoute> { GroupSettingScreen() }
        composable<MyPageGraph.SupportRoute> { SupportScreen() }
        composable<MyPageGraph.HealthGuideRoute> { HealthGuideScreen() }
    }
}
