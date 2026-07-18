package com.makeus.mody.feature.record.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.makeus.mody.core.designsystem.icon.ModyIcons
import kotlinx.coroutines.launch

/**
 * 촬영 단계 레이어: 풀스크린 프리뷰 + 우상단 닫기 + 하단(갤러리 / 셔터 / 전후면 전환).
 * 셔터 → 촬영·업라이트 정규화 후 [onCaptured] 로 결과 전달.
 */
@Composable
fun CaptureLayer(
    onCaptured: (UprightImage) -> Unit,
    onPickGallery: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing) {
        imageCapture = bindCamera(context, lifecycleOwner, previewView, lensFacing)
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Icon(
            painter = painterResource(ModyIcons.Plus1),
            contentDescription = "닫기",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(28.dp)
                .clickable(onClick = onClose),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 갤러리
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable(onClick = onPickGallery),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ModyIcons.Image),
                    contentDescription = "갤러리에서 선택",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // 셔터
            ShutterButton(
                enabled = !isCapturing,
                onClick = {
                    val capture = imageCapture ?: return@ShutterButton
                    isCapturing = true
                    scope.launch {
                        try {
                            val file = createRawFile(context)
                            val path = capture.capture(context, file)
                            onCaptured(normalizeToUpright(context, path))
                        } catch (_: Exception) {
                            // 실패 시 다시 촬영 가능하도록 상태만 복구
                        } finally {
                            isCapturing = false
                        }
                    }
                },
            )

            // 전후면 전환
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ModyIcons.Exchange),
                    contentDescription = "카메라 전환",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    Icon(
        painter = painterResource(ModyIcons.Shutter),
        contentDescription = "촬영",
        tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
        modifier = Modifier
            .size(64.dp)
            // 아이콘만 있어 기본 리플이 네모로 떠 어색 → 리플 제거.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
    )
}
