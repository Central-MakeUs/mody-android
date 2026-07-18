package com.makeus.mody.presentation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.presentation.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var navigationHelper: NavigationHelper
    @Inject lateinit var inviteCodeHolder: InviteCodeHolder

    // 앱은 다크모드를 고려하지 않음 → 기기 설정과 무관하게 항상 라이트로 강제.
    // (상태바 아이콘·force-dark 가 다크 따라가 흰 배경에서 깨지는 것 방지)
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                Configuration.UI_MODE_NIGHT_NO
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 초대 링크로 실행된 경우 코드 보관 → 그룹 참여 화면에서 소비.
        handleInviteDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                navigationHelper.navigationFlow.collect { event ->
                    when (event) {
                        is NavigationEvent.To -> navController.navigate(event.route) {
                            if (event.popUpTo) popUpTo(0) { inclusive = true }
                            // 같은 목적지가 스택 최상단이면 중복 push 방지(빠른 연타 대응)
                            launchSingleTop = true
                        }
                        is NavigationEvent.Up -> navController.navigateUp()
                        is NavigationEvent.TopLevelTo -> navController.navigate(event.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }

            ModyTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val startRoute by mainViewModel.startRoute.collectAsState()

                // startRoute 판정 전에는 스플래시(빈 화면). 판정되면 그 목적지로 NavHost 구성.
                val route = startRoute
                if (route == null) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(ModyTheme.colors.white))
                } else {
                    AppNavHost(navController = navController, startDestination = route)
                }
            }
        }
    }

    // 앱 실행 중 새 초대 링크 수신(singleTop).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInviteDeepLink(intent)
    }

    /**
     * 초대 코드 추출 후 보관. 두 경로를 처리한다:
     *  - App Link: https://dev-mody.store/invite?code=XXX
     *  - 카카오톡 공유 executionParams: kakao{네이티브키}://kakaolink?code=XXX
     */
    private fun handleInviteDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val isInviteLink = data.host == INVITE_HOST
        val isKakaoLink =
            data.scheme.orEmpty().startsWith(KAKAO_SCHEME_PREFIX) && data.host == KAKAO_LINK_HOST
        if (!isInviteLink && !isKakaoLink) return
        val code = data.getQueryParameter("code")?.takeIf { it.isNotBlank() } ?: return
        inviteCodeHolder.set(code)
    }

    private companion object {
        const val INVITE_HOST = "dev-mody.store"
        const val KAKAO_SCHEME_PREFIX = "kakao"
        const val KAKAO_LINK_HOST = "kakaolink"
    }
}
