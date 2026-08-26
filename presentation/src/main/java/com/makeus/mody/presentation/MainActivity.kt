package com.makeus.mody.presentation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import android.net.Uri
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.domain.analytics.AnalyticsLogger
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.invite.InviteCodeHolder
import com.makeus.mody.core.domain.notification.NotificationDeepLink
import com.makeus.mody.core.domain.notification.NotificationDeepLinkHolder
import com.makeus.mody.core.domain.notification.PendingGroupSelectionHolder
import com.makeus.mody.core.navigation.MainRoute
import com.makeus.mody.core.navigation.MyPageGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.Route
import com.makeus.mody.presentation.analytics.MainScreenName
import com.makeus.mody.presentation.analytics.toScreenName
import com.makeus.mody.presentation.navigation.AppNavHost
import com.makeus.mody.core.navigation.NotificationDestination
import com.makeus.mody.core.navigation.NotificationLinkParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // setContent 안의 hiltViewModel() 과 같은 인스턴스(둘 다 Activity 의 ViewModelStore 사용).
    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var navigationHelper: NavigationHelper
    @Inject lateinit var analyticsLogger: AnalyticsLogger
    @Inject lateinit var inviteCodeHolder: InviteCodeHolder
    @Inject lateinit var notificationDeepLinkHolder: NotificationDeepLinkHolder
    @Inject lateinit var pendingGroupSelectionHolder: PendingGroupSelectionHolder

    // 알림 권한(13+)은 온보딩 PermissionScreen 한 곳에서만 요청한다.
    // 여기서 선제 요청하면 온보딩 화면 도달 전에 팝업을 소비해(거부 시 2회째부터
    // 시스템이 다이얼로그를 안 띄움) 온보딩 요청이 무시되는 것처럼 보인다.

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

    // 앱 진입마다 오늘 걸음 수를 서버에 반영. onCreate 가 아니라 onStart 여야
    // 백그라운드에 두고 걷다가 돌아온 경우도 잡힌다.
    override fun onStart() {
        super.onStart()
        mainViewModel.onAppEntered()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 초대 링크로 실행된 경우 코드 보관 → 그룹 참여 화면에서 소비.
        handleInviteDeepLink(intent)
        // 알림 탭으로 실행된 경우 딥링크 보관 → NavHost 준비 후 소비.
        handleNotificationIntent(intent)
        // Health Connect "이 앱이 데이터를 사용하는 방법" 진입.
        // savedInstanceState 가 있으면 회전 등 재생성이라 이미 이동했다 — 다시 밀지 않는다.
        if (savedInstanceState == null) handleHealthRationaleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            // NavHost 가 그래프를 붙이기 전에 navigate 하면 "Navigation graph has not been set"
            // 으로 죽는다. 준비된 뒤부터 소비한다 — 그 전에 발행된 이벤트는 Channel(BUFFERED)에
            // 남아 있다가 그대로 전달되므로 유실되지 않는다.
            // (세션 만료처럼 스플래시 단계에서 이미 올라와 있을 수 있는 이벤트가 여기 해당한다.)
            var navHostReady by remember { mutableStateOf(false) }

            LaunchedEffect(navHostReady) {
                if (!navHostReady) return@LaunchedEffect
                navigationHelper.navigationFlow.collect { event ->
                    when (event) {
                        is NavigationEvent.To -> navController.navigate(event.route) {
                            if (event.popUpTo) popUpTo(0) { inclusive = true }
                            // 같은 목적지가 스택 최상단이면 중복 push 방지(빠른 연타 대응)
                            launchSingleTop = true
                        }
                        is NavigationEvent.Up -> navController.navigateUp()
                        is NavigationEvent.Replace -> {
                            // 이벤트를 낸 화면이 곧 현재 목적지다. 그 id 로 pop(inclusive) 하면서
                            // 새 목적지를 push — 결과적으로 스택 최상단만 갈아끼운다.
                            val currentId = navController.currentDestination?.id
                            navController.navigate(event.route) {
                                if (currentId != null) popUpTo(currentId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is NavigationEvent.BackTo -> {
                            // 대상 라우트까지 pop(대상 유지). 스택에 없으면 Up 폴백.
                            val popped = navController.popBackStack(event.route, inclusive = false)
                            if (!popped) navController.navigateUp()
                        }
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

            // 화면 노출 로그. Compose 는 자동 수집이 Activity 단위(전부 MainActivity)라
            // 여기서 직접 남긴다. 목적지 하나만 보면 되므로 화면마다 붙이지 않는다.
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { entry ->
                    val screenName = entry.destination.route.toScreenName() ?: return@collect
                    // Main 은 컨테이너라 실제로 보이는 건 탭이다 — MainScreen 이 탭 이름으로 남긴다.
                    if (screenName == MainScreenName) return@collect
                    analyticsLogger.logScreenView(screenName)
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
                    // 합성이 끝나야 그래프가 붙는다. 이 시점부터 네비게이션 이벤트를 소비한다.
                    LaunchedEffect(Unit) { navHostReady = true }
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
        handleHealthRationaleIntent(intent)
        // 앱 실행 중 알림 탭: 이미 NavHost 준비됨 → 즉시 소비.
        consumeNotificationDeepLink()
    }

    /**
     * Health Connect 권한 사용 근거 화면 요청 처리.
     *
     * Health Connect 앱/시스템 설정의 "이 앱이 데이터를 사용하는 방법"이 이 액션으로 앱을 연다
     * (Android 14+ 는 `VIEW_PERMISSION_USAGE`, 그 이하는 androidx 액션). 매니페스트에 필터만
     * 있고 이동이 없으면 앱 홈이 열려, 어떤 데이터를 왜 쓰는지 확인할 수가 없다.
     *
     * NavHost 가 아직 안 붙었어도 된다 — NavigationHelper 의 Channel(BUFFERED)이 들고 있다가
     * 준비된 뒤 전달한다. 로그인 여부와 무관하게 보여준다(근거 안내는 세션과 상관없다).
     */
    private fun handleHealthRationaleIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action != ACTION_SHOW_PERMISSIONS_RATIONALE &&
            action != ACTION_VIEW_PERMISSION_USAGE
        ) {
            return
        }
        // 액션을 지운다 — 남겨두면 Activity 재생성 시 같은 인텐트를 다시 읽어 또 이동한다.
        intent.action = null
        navigationHelper.navigate(NavigationEvent.To(MyPageGraph.HealthRationaleRoute))
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

    /**
     * 초대 코드 추출 후 보관. 두 경로를 처리한다:
     *  - App Link: https://{inviteHost}/invite?code=XXX (debug=dev-mody.store, release=prod-mody.shop)
     *  - 카카오톡 공유 executionParams: kakao{네이티브키}://kakaolink?code=XXX
     *
     * host 는 검사하지 않는다. manifest intent-filter 의 ${inviteHost} 가 buildType 별로
     * 치환되며 매칭되지 않는 링크는 애초에 전달되지 않으므로, 코드에서 도메인을 다시 고정하면
     * placeholder 와 어긋나 초대 코드를 놓친다.
     */
    private fun handleInviteDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val isInviteLink =
            data.scheme == INVITE_SCHEME && data.path.orEmpty().startsWith(INVITE_PATH_PREFIX)
        val isKakaoLink =
            data.scheme.orEmpty().startsWith(KAKAO_SCHEME_PREFIX) && data.host == KAKAO_LINK_HOST
        if (!isInviteLink && !isKakaoLink) return
        val code = data.getQueryParameter("code")?.takeIf { it.isNotBlank() } ?: return
        inviteCodeHolder.set(code)
    }

    private companion object {
        const val INVITE_SCHEME = "https"

        /** manifest intent-filter 의 android:pathPrefix 와 동일하게 유지할 것. */
        const val INVITE_PATH_PREFIX = "/invite"
        const val KAKAO_SCHEME_PREFIX = "kakao"
        const val KAKAO_LINK_HOST = "kakaolink"

        /**
         * Health Connect 권한 근거 화면 진입 액션. presentation manifest 의 intent-filter 와
         * 같은 값을 유지할 것.
         *
         * 문자열로 둔다 — `Intent.ACTION_VIEW_PERMISSION_USAGE` 는 API 29 상수라 minSdk 26 에서
         * 참조하면 lint 가 걸고, androidx 쪽 액션은 아예 상수가 없다.
         */
        const val ACTION_SHOW_PERMISSIONS_RATIONALE =
            "androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"
        const val ACTION_VIEW_PERMISSION_USAGE = "android.intent.action.VIEW_PERMISSION_USAGE"
    }
}
