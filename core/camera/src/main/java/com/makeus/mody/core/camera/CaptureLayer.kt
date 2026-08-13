package com.makeus.mody.core.camera

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CAPTURE_FAILED_MESSAGE = "사진을 찍지 못했어요. 다시 시도해주세요."

/**
 * 촬영 단계 레이어: 풀스크린 프리뷰 + 우상단 닫기 + 하단(갤러리 / 셔터 / 전후면 전환).
 * 셔터 → 촬영·업라이트 정규화 후 [onCaptured] 로 결과 전달.
 *
 * @param onPickGallery null 이면 갤러리 버튼을 숨긴다. 자리는 비워 둬 셔터가 가운데에 남는다.
 */
@Composable
fun CaptureLayer(
    onCaptured: (UprightImage) -> Unit,
    onPickGallery: (() -> Unit)?,
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
            // 갤러리. 안 쓰는 화면에선 빈 자리만 남겨 셔터가 가운데에 머물게 한다.
            if (onPickGallery != null) {
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
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }

            // 셔터
            ShutterButton(
                enabled = !isCapturing,
                onClick = {
                    val capture = imageCapture ?: return@ShutterButton
                    isCapturing = true
                    scope.launch {
                        // 실패 시 지울 수 있도록 try 밖에서 붙든다. 성공하면 normalizeToUpright 가
                        // 원본을 대체하며 지우므로 여기서는 비운다.
                        var rawPath: String? = null
                        try {
                            val file = createRawFile(context)
                            rawPath = file.absolutePath
                            val path = capture.capture(context, file)
                            // 디코딩·회전·재인코딩은 무거워 메인 스레드에서 하면 셔터가 멈춘다.
                            val upright = withContext(Dispatchers.IO) {
                                normalizeToUpright(context, path)
                            }
                            rawPath = null
                            onCaptured(upright)
                        } catch (e: CancellationException) {
                            // 화면을 벗어나 스코프가 취소된 경우. 삼키면 아래 안내가 잘못 뜬다.
                            // 남은 파일은 다음 진입의 오래된 캐시 정리가 걷어간다.
                            throw e
                        } catch (_: Exception) {
                            // 조용히 넘기면 셔터를 눌러도 아무 반응이 없어 고장으로 보인다.
                            Toast.makeText(context, CAPTURE_FAILED_MESSAGE, Toast.LENGTH_SHORT).show()
                            rawPath?.let { withContext(Dispatchers.IO) { deleteCameraFile(it) } }
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
