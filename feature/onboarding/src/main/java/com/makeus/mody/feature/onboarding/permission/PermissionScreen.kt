package com.makeus.mody.feature.onboarding.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.permission.contract.PermissionIntent

/** 화면에 표기하고 요청할 접근 권한 항목. */
private data class PermissionItem(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
)

private val BasePermissionItems = listOf(
    PermissionItem(ModyIcons.Alarm, "알림", "버디 인증, 응원 댓글, 챌린지 달성 소식 받기"),
    PermissionItem(ModyIcons.Camera, "카메라", "오늘의 식사와 운동을 사진으로 기록"),
    PermissionItem(ModyIcons.Image, "사진", "갤러리에서 식단·운동 사진을 바로 불러오기"),
)

/** 건강 정보(걸음 수 챌린지)는 Phase 2 기능 — 플래그가 열렸을 때만 표기. */
private val HealthPermissionItem = PermissionItem(ModyIcons.Exercise, "건강 정보", "걸음 수 챌린지")

/**
 * "확인" 시 실제로 요청할 런타임 권한. 전부 선택(거부해도 진행).
 *
 * 사진은 Photo Picker(PickVisualMedia)라 권한 불요.
 * 걸음 수는 Health Connect 권한이라 여기 못 넣는다 — 일반 런타임 권한이 아니라
 * [PermissionController.createRequestPermissionResultContract] 로 따로 요청해야 하고,
 * 화면도 별개라 알림·카메라 요청이 끝난 뒤 순차로 띄운다.
 */
private fun requestedPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
    add(Manifest.permission.CAMERA)
}.toTypedArray()

@Composable
fun PermissionScreen(viewModel: PermissionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // 결과(허용/거부)와 무관하게 다음 단계로 — 전부 선택 권한.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.onIntent(PermissionIntent.BasePermissionsHandled) }

    // Health Connect 권한 요청. 허용 집합을 그대로 돌려주므로 요청한 권한이 전부 포함됐는지로 판정.
    // 요청 집합은 화면 로컬에 따로 붙든다 — 런처 콜백은 항상 최신 값을 읽으므로,
    // 런처를 띄운 직후 비우는 state 를 그대로 참조하면 결과가 늘 '거부'로 판정된다.
    var pendingHealthPermissions by remember { mutableStateOf<Set<String>?>(null) }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions ->
        val requested = pendingHealthPermissions
        pendingHealthPermissions = null
        viewModel.onIntent(
            PermissionIntent.HealthPermissionResult(
                granted = requested != null && grantedPermissions.containsAll(requested),
            ),
        )
    }
    LaunchedEffect(state.healthPermissionRequest) {
        val permissions = state.healthPermissionRequest
        if (permissions.isNullOrEmpty()) return@LaunchedEffect
        pendingHealthPermissions = permissions
        // 권한 화면을 못 여는 기기가 있다. 던지면 온보딩이 이 화면에 갇히므로 거부로 이어간다.
        val launched = runCatching { healthPermissionLauncher.launch(permissions) }.isSuccess
        viewModel.onIntent(PermissionIntent.HealthPermissionRequestLaunched)
        if (!launched) {
            pendingHealthPermissions = null
            viewModel.onIntent(PermissionIntent.HealthPermissionResult(granted = false))
        }
    }

    PermissionContent(
        // 표기 조건 = 실제 요청 조건(Phase 2 플래그 + 기기 지원). ViewModel 이 같은 값으로 분기한다.
        showHealth = state.showHealth,
        onConfirm = { permissionLauncher.launch(requestedPermissions()) },
    )
}

@Composable
private fun PermissionContent(showHealth: Boolean, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp),
    ) {
        // 온보딩/그룹(GroupScaffold TitleTopOffset)과 동일한 상단→타이틀 간격 72dp.
        Spacer(modifier = Modifier.height(72.dp))
        Text(
            text = "MODY를 이용하려면\n다음 접근 권한 허용이 필요해요",
            style = ModyTheme.typography.h2,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "선택 권한을 허용하지 않아도 사용할 수 있어요.\n해당 기능을 이용할 때 다시 요청드릴게요.",
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray06,
        )

        Spacer(modifier = Modifier.height(40.dp))
        val items = if (showHealth) BasePermissionItems + HealthPermissionItem else BasePermissionItems
        items.forEach { item ->
            PermissionRow(item)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        ModyButton(
            text = "확인",
            onClick = onConfirm,
            variant = ModyButtonVariant.Primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun PermissionRow(item: PermissionItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ModyTheme.colors.gray01),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = ModyTheme.colors.gray06,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                )
                Text(
                    text = " (선택)",
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.gray05,
            )
        }
    }
}
