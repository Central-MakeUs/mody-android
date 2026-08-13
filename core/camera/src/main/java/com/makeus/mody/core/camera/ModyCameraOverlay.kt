package com.makeus.mody.core.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.CropRegion

/** 기록 카드 비율(354:200). */
const val RECORD_FRAME_RATIO = 200f / 354f

/** 정사각(1:1). 주간 챌린지 인증 사진 격자. */
const val SQUARE_FRAME_RATIO = 1f

/**
 * 커스텀 촬영 오버레이(앱 공용). 풀스크린으로 띄운다.
 * 권한 → 촬영 → 조정 → [onConfirm] 으로 원본 uri + 크롭 영역 전달.
 *
 * @param frameRatio 조정 단계 크롭 프레임의 높이/너비. 쓰는 곳의 카드 비율을 넘긴다
 *   ([RECORD_FRAME_RATIO], [SQUARE_FRAME_RATIO]).
 * @param onConfirm (원본 이미지 uri, 정규화 크롭 영역).
 * @param onPickGallery 갤러리 버튼 → 시스템 갤러리 열기(호출부의 런처). null 이면 버튼을 숨긴다.
 * @param onDismiss 닫기/취소.
 */
@Composable
fun ModyCameraOverlay(
    frameRatio: Float,
    onConfirm: (uri: String, cropRegion: CropRegion) -> Unit,
    onPickGallery: (() -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current

    // 오버레이 표시 동안 하단 내비게이션 바 숨김(immersive). 벗어나면 원복.
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        controller?.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.navigationBars()) }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf<UprightImage?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        // 이전 촬영에서 남은 캐시 정리. 진입 시점이라 이번 세션 파일은 아직 없다.
        clearStaleCameraCache(context)
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when {
            !hasPermission -> PermissionPrompt(
                denied = permissionDenied,
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onClose = onDismiss,
            )

            captured == null -> CaptureLayer(
                onCaptured = { captured = it },
                onPickGallery = onPickGallery,
                onClose = onDismiss,
            )

            else -> AdjustLayer(
                image = captured!!,
                frameRatio = frameRatio,
                onRetake = {
                    // 버린 촬영본은 바로 지운다. 재촬영을 반복하면 그만큼 캐시가 쌓인다.
                    deleteCameraFile(captured!!.path)
                    captured = null
                },
                onConfirm = onConfirm,
                onClose = onDismiss,
            )
        }
    }
}

/** ContextWrapper 체인을 따라 호스트 Activity 를 찾는다(내비바 제어용 window 접근). */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun PermissionPrompt(
    denied: Boolean,
    onRequest: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "카메라 권한이 필요해요",
            style = ModyTheme.typography.b3,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (denied) "설정에서 카메라 권한을 허용해주세요." else "촬영을 위해 카메라 권한을 허용해주세요.",
            style = ModyTheme.typography.b6,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ModyTheme.colors.primary100)
                .clickable(onClick = onRequest)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(text = "권한 요청", style = ModyTheme.typography.b5, color = ModyTheme.colors.gray10)
        }
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable(onClick = onClose)
                .padding(12.dp),
        ) {
            Text(text = "닫기", style = ModyTheme.typography.b6, color = Color.White)
        }
    }
}
