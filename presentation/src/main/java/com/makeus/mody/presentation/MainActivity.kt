package com.makeus.mody.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.domain.notification.NotificationDeepLink
import com.makeus.mody.core.domain.notification.NotificationDeepLinkHolder
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.core.navigation.Route
import com.makeus.mody.presentation.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var navigationHelper: NavigationHelper
    @Inject lateinit var inviteCodeHolder: InviteCodeHolder
    @Inject lateinit var notificationDeepLinkHolder: NotificationDeepLinkHolder

    // 13+ 알림 권한 요청. 거부해도 앱은 그대로 진행(알림만 안 뜸).
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // 확정된 시작 목적지. 알림 딥링크는 메인(로그인 완료) 상태에서만 소비한다.
    private var resolvedStartRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 초대 링크로 실행된 경우 코드 보관 → 그룹 참여 화면에서 소비.
        handleInviteDeepLink(intent)
        // 알림 탭으로 실행된 경우 딥링크 보관 → NavHost 준비 후 소비.
        handleNotificationIntent(intent)
        requestNotificationPermissionIfNeeded()
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
                // NavHost 준비(시작 목적지 확정) 후 알림 딥링크 1회 소비 → 라우팅.
                LaunchedEffect(route) {
                    resolvedStartRoute = route
                    if (route != null) consumeNotificationDeepLink()
                }
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
        handleNotificationIntent(intent)
        // 앱 실행 중 알림 탭: 이미 NavHost 준비됨 → 즉시 소비.
        consumeNotificationDeepLink()
    }

    /** 알림 PendingIntent extra 에서 딥링크 정보 추출 → 홀더 보관(1회성). */
    private fun handleNotificationIntent(intent: Intent?) {
        val type = intent?.getStringExtra(NotificationDeepLink.KEY_TYPE) ?: return
        val targetId = intent.getStringExtra(NotificationDeepLink.KEY_TARGET_ID)?.toLongOrNull()
        notificationDeepLinkHolder.set(NotificationDeepLink(type = type, targetId = targetId))
        // 소비 후 extra 제거 → 화면 회전 등 Activity 재생성 시 재파싱·재이동(엉뚱한 화면으로 튐) 방지.
        intent.removeExtra(NotificationDeepLink.KEY_TYPE)
        intent.removeExtra(NotificationDeepLink.KEY_TARGET_ID)
    }

    /**
     * 보관된 알림 딥링크가 있으면 해당 화면으로 이동.
     * 단 메인(로그인 완료) 진입 상태에서만 — 로그인/온보딩/그룹 단계면 스택 오염 막으려 폐기만 한다.
     */
    private fun consumeNotificationDeepLink() {
        if (resolvedStartRoute != MainRoute) {
            notificationDeepLinkHolder.consume()
            return
        }
        val deepLink = notificationDeepLinkHolder.consume() ?: return
        navigationHelper.navigate(NavigationEvent.To(deepLink.toRoute()))
    }

    /** 알림 종류 → 이동할 화면. 새 타입은 여기 when 에 한 줄 추가. */
    private fun NotificationDeepLink.toRoute(): Route = when (type) {
        // TODO(notification): 타입별 상세 분기. 예) "COMMENT" → RecordDetailRoute(groupId, targetId)
        else -> NotificationGraph.NotificationRoute
    }

    /** Android 13+ 에서 알림 권한 미허용이면 런타임 요청. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
