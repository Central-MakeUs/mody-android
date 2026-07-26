package com.makeus.mody.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import android.net.Uri
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.domain.notification.NotificationDeepLink
import com.makeus.mody.core.domain.notification.NotificationDeepLinkHolder
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.Route
import com.makeus.mody.presentation.navigation.AppNavHost
import com.makeus.mody.presentation.notification.NotificationDestination
import com.makeus.mody.presentation.notification.NotificationLinkParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var navigationHelper: NavigationHelper
    @Inject lateinit var inviteCodeHolder: InviteCodeHolder
    @Inject lateinit var notificationDeepLinkHolder: NotificationDeepLinkHolder
    @Inject lateinit var pendingGroupSelectionHolder: PendingGroupSelectionHolder

    // 13+ 알림 권한 요청. 거부해도 앱은 그대로 진행(알림만 안 뜸).
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // 확정된 시작 목적지. 알림 딥링크는 메인(로그인 완료) 상태에서만 소비한다.
    private var resolvedStartRoute: Route? = null

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
                val gate by mainViewModel.splashGate.collectAsState()

                // startRoute 판정 전·게이트 통과 전에는 스플래시(빈 화면) 유지.
                val route = startRoute
                val gatePassed = gate is SplashGateState.Passed
                // NavHost 준비(시작 목적지 확정 + 게이트 통과) 후 알림 딥링크 1회 소비 → 라우팅.
                LaunchedEffect(route, gatePassed) {
                    resolvedStartRoute = route
                    if (route != null && gatePassed) consumeNotificationDeepLink()
                }
                if (route == null || !gatePassed) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(ModyTheme.colors.white))
                } else {
                    AppNavHost(navController = navController, startDestination = route)
                }

                // 스플래시 게이트 다이얼로그(iOS 와 동일 순서: 강제 업데이트 → 최소 버전 → 공지).
                when (val g = gate) {
                    is SplashGateState.UpdateRequired -> ModyDialog(
                        title = "업데이트가 필요해요",
                        message = "원활한 이용을 위해 최신 버전으로 업데이트해주세요.",
                        confirmText = "업데이트하기",
                        onConfirm = { openStore(g.storeUrl) },
                        onDismissRequest = {}, // 백키/스크림으로 우회 불가
                    )
                    is SplashGateState.Notice -> ModyDialog(
                        title = g.notice.title,
                        message = g.notice.message,
                        confirmText = "확인",
                        confirmEnabled = g.notice.skipPossible,
                        onConfirm = mainViewModel::confirmNotice,
                        onDismissRequest = {}, // 진행 여부는 skipPossible 이 결정
                    )
                    else -> Unit
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
        val link = intent?.getStringExtra(NotificationDeepLink.KEY_LINK) ?: return
        notificationDeepLinkHolder.set(NotificationDeepLink(link = link))
        // 소비 후 extra 제거 → 화면 회전 등 Activity 재생성 시 재파싱·재이동(엉뚱한 화면으로 튐) 방지.
        intent.removeExtra(NotificationDeepLink.KEY_LINK)
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
        when (val dest = NotificationLinkParser.parse(deepLink.link)) {
            is NotificationDestination.Screen ->
                navigationHelper.navigate(NavigationEvent.To(dest.route))
            // 그룹홈은 별도 라우트가 없어 Feed 탭 + 그룹 전환으로 처리(홀더에 groupId 보관).
            // MainScreenViewModel(탭 전환) + FeedViewModel(그룹 선택) 이 반응한다.
            is NotificationDestination.GroupHome ->
                pendingGroupSelectionHolder.set(dest.groupId)
            // 미지원 경로: 무시(현재 화면 유지).
            null -> Unit
        }
    }

    /** 스토어로 이동. 원격 URL 없으면 마켓 스킴, 마켓 미설치면 웹 스토어 폴백. */
    private fun openStore(url: String?) {
        val target = url?.takeIf { it.isNotBlank() } ?: "market://details?id=$packageName"
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target))) }
            .onFailure {
                runCatching {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                        ),
                    )
                }
            }
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
