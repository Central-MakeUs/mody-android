package com.makeus.mody.core.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.makeus.mody.core.model.CropRegion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 기록 카드 비율(354:200). */
const val RECORD_FRAME_RATIO = 200f / 354f

/** 정사각(1:1). 주간 챌린지 인증 사진 격자. */
const val SQUARE_FRAME_RATIO = 1f

private const val ROTATE_FAILED_MESSAGE = "사진을 저장하지 못했어요. 다시 시도해주세요."
private const val GALLERY_FAILED_MESSAGE = "사진을 불러오지 못했어요. 다른 사진을 선택해주세요."

/** 오버레이를 어디서 시작할지. */
enum class CameraSource {
    /** 촬영 화면부터. */
    CAMERA,

    /** 시스템 사진 선택기부터. 카메라 권한을 묻지 않는다. 선택기를 닫으면 오버레이도 닫힌다. */
    GALLERY,
}

/**
 * 커스텀 촬영 오버레이(앱 공용). 풀스크린으로 띄운다.
 * (권한 → 촬영(3:4) | 갤러리 선택) → 조정(프레임 위치·90° 회전) → [onConfirm] 으로 원본 uri + 크롭 영역 전달.
 * 촬영본과 갤러리 사진은 같은 조정 화면을 거치므로 크롭 영역은 항상 있다.
 * 회전했으면 원본은 회전이 구워진 파일이고, 크롭 영역도 그 기준이다.
 *
 * 갤러리 선택기는 여기 안에서 연다 — 호출부가 열면 결과가 조정 화면을 건너뛴다.
 *
 * @param frameRatio 조정 단계 크롭 프레임의 높이/너비. 쓰는 곳의 카드 비율을 넘긴다
 *   ([RECORD_FRAME_RATIO], [SQUARE_FRAME_RATIO]).
 * @param onConfirm (원본 이미지 uri, 정규화 크롭 영역).
 * @param allowGallery false 면 촬영 화면의 갤러리 버튼을 숨긴다(인증 사진처럼 그 자리에서 찍어야 할 때).
 * @param onDismiss 닫기/취소.
 * @param initialSource 시작 화면. [CameraSource.GALLERY] 는 [allowGallery] 가 true 일 때만 쓴다.
 */
@Composable
fun ModyCameraOverlay(
    frameRatio: Float,
    onConfirm: (uri: String, cropRegion: CropRegion) -> Unit,
    allowGallery: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialSource: CameraSource = CameraSource.CAMERA,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

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
    // 조정 중인 사진이 갤러리에서 왔는지. "다시 찍기" 대신 "다시 선택" 으로 선택기를 연다.
    var capturedFromGallery by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    // 갤러리로 시작했는데 고른 사진이 없으면 보여줄 화면이 없다 → 닫는다.
    // 촬영 화면에서 열었거나, 조정 중 "다시 선택" 에서 열었으면 그 화면으로 돌아간다.
    fun dismissIfNothingToShow() {
        if (captured == null && initialSource == CameraSource.GALLERY) onDismiss()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            dismissIfNothingToShow()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                val imported = withContext(Dispatchers.IO) { importToUpright(context, uri) }
                // 새 사진이 준비된 뒤에 바꾼다 — 실패하면 보던 사진이 그대로 남는다.
                // 회전·프레임 위치는 AdjustLayer 가 uri 로 붙들고 있어 새 사진에서 초기화된다.
                val discarded = captured?.path
                captured = imported
                capturedFromGallery = true
                if (discarded != null) withContext(Dispatchers.IO) { deleteCameraFile(discarded) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Toast.makeText(context, GALLERY_FAILED_MESSAGE, Toast.LENGTH_SHORT).show()
                dismissIfNothingToShow()
            }
        }
    }
    val openGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(Unit) {
        // 이전 촬영에서 남은 캐시 정리. 진입 시점이라 이번 세션 파일은 아직 없다.
        // LaunchedEffect 는 컴포지션 컨텍스트(메인)에서 도니 디렉터리 스캔·삭제는 IO 로 옮긴다.
        withContext(Dispatchers.IO) { clearStaleCameraCache(context) }
        when {
            initialSource == CameraSource.GALLERY -> openGallery()
            !hasPermission -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val image = captured
        when {
            // 갤러리 사진은 카메라 권한 없이도 조정할 수 있어야 해서 권한보다 먼저 본다.
            image != null -> AdjustLayer(
                image = image,
                frameRatio = frameRatio,
                retakeLabel = if (capturedFromGallery) "다시 선택" else "다시 찍기",
                confirming = confirming,
                onRetake = {
                    if (capturedFromGallery) {
                        // 새 사진을 고를 때까지 지금 사진을 유지한다. 선택기를 닫으면 그대로 이어서 편집.
                        openGallery()
                    } else {
                        // 버린 촬영본은 바로 지운다. 재촬영을 반복하면 그만큼 캐시가 쌓인다.
                        // 경로를 먼저 붙들고 상태를 비운다 — 삭제는 IO 로 넘겨 화면 전환을 막지 않는다.
                        val discarded = image.path
                        captured = null
                        scope.launch { withContext(Dispatchers.IO) { deleteCameraFile(discarded) } }
                    }
                },
                onConfirm = { quarterTurns, region ->
                    if (quarterTurns == 0) {
                        onConfirm(image.uri, region)
                        return@AdjustLayer
                    }
                    confirming = true
                    scope.launch {
                        try {
                            // 디코딩·회전·재인코딩은 무거워 메인에서 하면 화면이 멈춘다.
                            val rotated = withContext(Dispatchers.IO) {
                                rotateUpright(context, image, quarterTurns * 90)
                            }
                            // 회전 전 파일은 rotateUpright 가 지웠다. 상태도 새 파일로 바꿔 둬야
                            // 이후 다시 찍기가 지워진 경로를 지우려 하지 않는다.
                            captured = rotated
                            onConfirm(rotated.uri, region)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // 조용히 넘기면 업로드를 눌러도 반응이 없어 고장으로 보인다.
                            Toast.makeText(context, ROTATE_FAILED_MESSAGE, Toast.LENGTH_SHORT).show()
                        } finally {
                            confirming = false
                        }
                    }
                },
                onClose = onDismiss,
            )

            // 갤러리로 시작해 선택기가 떠 있거나 사진을 들여오는 중. 비워 둔다.
            initialSource == CameraSource.GALLERY -> Unit

            !hasPermission -> PermissionPrompt(
                denied = permissionDenied,
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onClose = onDismiss,
            )

            else -> CaptureLayer(
                onCaptured = {
                    captured = it
                    capturedFromGallery = false
                },
                onPickGallery = if (allowGallery) openGallery else null,
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
