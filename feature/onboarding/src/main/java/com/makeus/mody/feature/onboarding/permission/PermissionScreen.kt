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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

private val PermissionItems = listOf(
    PermissionItem(ModyIcons.Alarm, "알림", "버디 인증, 응원 댓글, 챌린지 달성 소식 받기"),
    PermissionItem(ModyIcons.Camera, "카메라", "오늘의 식사와 운동을 사진으로 기록"),
    PermissionItem(ModyIcons.Image, "사진", "갤러리에서 식단·운동 사진을 바로 불러오기"),
    PermissionItem(ModyIcons.Exercise, "건강 정보", "걸음 수 챌린지"),
)

/** "확인" 시 실제로 요청할 런타임 권한. 전부 선택(거부해도 진행). */
private fun requestedPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
        add(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    add(Manifest.permission.CAMERA)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        add(Manifest.permission.ACTIVITY_RECOGNITION)
    }
}.toTypedArray()

@Composable
fun PermissionScreen(viewModel: PermissionViewModel = hiltViewModel()) {
    // 결과(허용/거부)와 무관하게 다음(그룹)으로 진입 — 전부 선택 권한.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.onIntent(PermissionIntent.Continue) }

    PermissionContent(
        onConfirm = { permissionLauncher.launch(requestedPermissions()) },
    )
}

@Composable
private fun PermissionContent(onConfirm: () -> Unit) {
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
        PermissionItems.forEach { item ->
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
