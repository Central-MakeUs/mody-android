package com.makeus.mody.feature.record.camera

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.CropRegion
import kotlin.math.roundToInt

/** 기록 카드 비율(354:200). */
private const val FRAME_RATIO = 200f / 354f

/**
 * 조정 단계: 촬영본을 세로로 드래그해 354:200 크롭 영역을 맞춘다.
 * 프레임은 화면 중앙 고정, 사진이 위아래로 움직인다. 프레임 밖은 어둡게.
 *
 * 원본은 그대로 업로드하고, 프레임 위치를 원본 대비 정규화 크롭 영역(CropRegion)으로 계산해 넘긴다.
 * 세로 슬라이스만 지원하므로 x=0, width=1, y/height 만 변한다.
 */
@Composable
fun AdjustLayer(
    image: UprightImage,
    onRetake: () -> Unit,
    onConfirm: (originalUri: String, cropRegion: CropRegion) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val density = LocalDensity.current
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        val frameW = screenW
        val frameH = frameW * FRAME_RATIO
        val baseFrameTop = (screenH - frameH) / 2f

        // 사진은 화면 중앙 고정. 프레임(사각형)이 위아래로 움직인다.
        val scale = screenW / image.width
        val dispH = image.height * scale
        val imageTop = (screenH - dispH) / 2f
        val imageBottom = imageTop + dispH

        // 프레임은 화면·사진 안에 머문다.
        val minFrameTop = maxOf(0f, imageTop)
        val maxFrameTop = minOf(screenH - frameH, imageBottom - frameH)
        val hasRoom = maxFrameTop >= minFrameTop
        val minOff = minFrameTop - baseFrameTop
        val maxOff = maxFrameTop - baseFrameTop

        var frameOffset by remember { mutableFloatStateOf(0f) }
        val frameTop =
            if (hasRoom) (baseFrameTop + frameOffset).coerceIn(minFrameTop, maxFrameTop) else baseFrameTop
        val frameBottom = frameTop + frameH

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(image.uri, hasRoom) {
                    if (!hasRoom) return@pointerInput
                    detectDragGestures { _, drag ->
                        frameOffset = (frameOffset + drag.y).coerceIn(minOff, maxOff)
                    }
                },
        ) {
            AsyncImage(
                model = image.uri,
                contentDescription = "촬영 사진",
                modifier = Modifier
                    .offset { IntOffset(0, imageTop.roundToInt()) }
                    .fillMaxWidth()
                    .height(with(density) { dispH.toDp() }),
            )

            // 프레임 밖 상/하단 딤.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { frameTop.toDp() })
                    .background(Color.Black.copy(alpha = 0.55f)),
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, frameBottom.roundToInt()) }
                    .fillMaxWidth()
                    .height(with(density) { (screenH - frameBottom).toDp() })
                    .background(Color.Black.copy(alpha = 0.55f)),
            )

            // 점선 프레임(옐로).
            val dashColor = ModyTheme.colors.primary100
            Canvas(
                modifier = Modifier
                    .offset { IntOffset(0, frameTop.roundToInt()) }
                    .fillMaxWidth()
                    .height(with(density) { frameH.toDp() }),
            ) {
                drawRect(
                    color = dashColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    style = Stroke(
                        width = with(density) { 2.dp.toPx() },
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                with(density) { 8.dp.toPx() },
                                with(density) { 6.dp.toPx() },
                            ),
                        ),
                    ),
                )
            }
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
            RetakeButton(onClick = onRetake)
            UploadButton(onClick = {
                // 프레임(표시 좌표)을 원본 픽셀 기준으로 환산 후 원본 높이로 정규화.
                val topInImage = (frameTop - imageTop) / scale
                val heightInImage = frameH / scale
                val y = (topInImage / image.height).coerceIn(0f, 1f)
                val h = (heightInImage / image.height).coerceIn(0f, 1f - y)
                onConfirm(image.uri, CropRegion(x = 0f, y = y, width = 1f, height = h))
            })
        }
    }
}

@Composable
private fun RetakeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC1A1A1A))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text = "다시 찍기", style = ModyTheme.typography.b6, color = Color.White)
    }
}

@Composable
private fun UploadButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.primary100)
            .clickable(onClick = onClick)
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
