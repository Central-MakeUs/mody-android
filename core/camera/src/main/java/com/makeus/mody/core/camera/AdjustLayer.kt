package com.makeus.mody.core.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import coil.transform.Transformation
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.model.CropRegion
import kotlin.math.roundToInt

/**
 * 조정 단계(촬영본·갤러리 사진 공용): 사진 위의 크롭 프레임을 드래그해 영역을 맞추고, 사진을 90° 씩 돌린다.
 *
 * 편집 영역은 촬영 프리뷰와 같은 3:4 박스(화면 폭 기준, 세로 중앙)다. 사진은 그 안에 잘리지 않고
 * 통째로 들어가게 맞추고(남는 곳은 여백), 프레임은 사진 안에서 상하좌우로 움직인다. 프레임 밖은 어둡게.
 * 배치 규칙은 [cropLayout] 참고.
 *
 * 원본은 그대로 업로드하고, 프레임 위치를 원본 대비 정규화 크롭 영역(CropRegion)으로 계산해 넘긴다.
 *
 * 회전은 화면에서는 표시만 돌리고, 파일에는 [onConfirm] 을 받은 쪽이 굽는다([rotateUpright]).
 * 크롭 영역은 회전된 사진 기준이다.
 *
 * @param frameRatio 프레임 높이/너비.
 * @param retakeLabel 왼쪽 아래 버튼 문구. 촬영본은 "다시 찍기", 갤러리 사진은 "다시 선택".
 * @param confirming 회전본을 굽는 중. 버튼을 막아 두 번 업로드되지 않게 한다.
 * @param onConfirm (시계 방향 회전 횟수 0~3, 회전된 사진 기준 크롭 영역).
 */
@Composable
internal fun AdjustLayer(
    image: UprightImage,
    frameRatio: Float,
    retakeLabel: String,
    confirming: Boolean,
    onRetake: () -> Unit,
    onConfirm: (quarterTurns: Int, cropRegion: CropRegion) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 다른 사진이 들어오면 회전·프레임 위치를 처음부터 시작한다.
    var quarterTurns by remember(image.uri) { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val density = LocalDensity.current
        val viewportW = constraints.maxWidth.toFloat()
        // 화면이 3:4 보다 짧은 기기(가로 모드·분할 화면)에선 화면 높이를 넘지 않는다.
        val viewportH = minOf(
            constraints.maxHeight.toFloat(),
            viewportW * CAPTURE_ASPECT_HEIGHT / CAPTURE_ASPECT_WIDTH,
        )
        val (imageW, imageH) = rotatedSize(image.width, image.height, quarterTurns)

        // 배치·크롭 계산은 CropGeometry 의 순수 함수에 있다(화면 크기·사진 비율 조합이
        // 많아 손으로 확인이 안 돼 테스트로 고정했다).
        val layout = remember(viewportW, viewportH, imageW, imageH, frameRatio) {
            cropLayout(
                viewportWidth = viewportW,
                viewportHeight = viewportH,
                imageWidth = imageW,
                imageHeight = imageH,
                frameRatio = frameRatio,
            )
        }

        // 회전하면 사진 크기가 바뀌어 이전 위치가 의미 없다 → 가운데로 되돌린다(iOS 와 같다).
        var offsetX by remember(image.uri, quarterTurns) { mutableFloatStateOf(0f) }
        var offsetY by remember(image.uri, quarterTurns) { mutableFloatStateOf(0f) }
        val frameLeft = layout.baseFrameLeft + offsetX
        val frameTop = layout.baseFrameTop + offsetY

        val context = LocalContext.current
        val request = remember(image.uri, quarterTurns) {
            ImageRequest.Builder(context)
                .data(image.uri)
                .apply { if (quarterTurns != 0) transformations(RotateTransformation(quarterTurns * 90)) }
                .build()
        }

        // 3:4 편집 영역. 좌표는 전부 이 박스 기준이다.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(with(density) { viewportH.toDp() })
                .pointerInput(layout) {
                    detectDragGestures { _, drag ->
                        val (x, y) = layout.clampOffset(offsetX + drag.x, offsetY + drag.y)
                        offsetX = x
                        offsetY = y
                    }
                },
        ) {
            // 사진 전체를 비율 그대로(aspect-fit). 크기를 cropLayout 이 계산한 표시 크기로 고정해
            // 그린 위치와 크롭 계산이 어긋나지 않게 한다.
            AsyncImage(
                model = request,
                contentDescription = "촬영 사진",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset { IntOffset(layout.imageLeft.roundToInt(), layout.imageTop.roundToInt()) }
                    .size(
                        width = with(density) { layout.displayedWidth.toDp() },
                        height = with(density) { layout.displayedHeight.toDp() },
                    ),
            )

            // 프레임 밖 딤 + 점선 프레임(옐로).
            val dashColor = ModyTheme.colors.primary100
            Canvas(modifier = Modifier.fillMaxSize()) {
                val frame = Rect(
                    offset = Offset(frameLeft, frameTop),
                    size = Size(layout.frameWidth, layout.frameHeight),
                )
                val dim = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, size))
                    addRect(frame)
                }
                drawPath(dim, color = Color.Black.copy(alpha = 0.55f))
                drawRect(
                    color = dashColor,
                    topLeft = frame.topLeft,
                    size = frame.size,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
                    ),
                )
            }
        }

        // 회전(좌상단): 반시계 / 시계 90°.
        // TODO: 디자인 확정 전 임시 아이콘 — 되돌리기(반시계) 아이콘을 그대로, 시계 방향은 좌우 반전해 쓴다.
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RotateButton(
                description = "왼쪽으로 회전",
                mirrored = false,
                enabled = !confirming,
                onClick = { quarterTurns = rotateQuarterTurn(quarterTurns, clockwise = false) },
            )
            RotateButton(
                description = "오른쪽으로 회전",
                mirrored = true,
                enabled = !confirming,
                onClick = { quarterTurns = rotateQuarterTurn(quarterTurns, clockwise = true) },
            )
        }

        // 닫기(우상단).
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

        // 하단 액션: 다시 찍기 / 업로드.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // 이전(기록) 화면 작성완료 버튼과 하단 여백 맞춤: 좌우 24dp, 하단 50dp.
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RetakeButton(label = retakeLabel, enabled = !confirming, onClick = onRetake)
            UploadButton(
                enabled = !confirming,
                onClick = { onConfirm(quarterTurns, layout.cropRegionAt(frameLeft, frameTop)) },
            )
        }
    }
}

/** 표시용 회전. 파일은 건드리지 않는다 — 확정 시 [rotateUpright] 가 굽는다. */
private class RotateTransformation(private val degrees: Int) : Transformation {
    override val cacheKey: String = "rotate-$degrees"

    override suspend fun transform(input: Bitmap, size: CoilSize): Bitmap =
        Bitmap.createBitmap(
            input, 0, 0, input.width, input.height,
            Matrix().apply { postRotate(degrees.toFloat()) }, true,
        )
}

@Composable
private fun RotateButton(description: String, mirrored: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Icon(
        painter = painterResource(ModyIcons.Reset),
        contentDescription = description,
        tint = Color.White,
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer { if (mirrored) scaleX = -1f }
            .clickable(enabled = enabled, onClick = onClick),
    )
}

@Composable
private fun RetakeButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC1A1A1A))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text = label, style = ModyTheme.typography.b6, color = Color.White)
    }
}

@Composable
private fun UploadButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.primary100)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = "업로드", style = ModyTheme.typography.b6, color = ModyTheme.colors.gray10)
        Icon(
            painter = painterResource(ModyIcons.CheckFill),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
    }
}
