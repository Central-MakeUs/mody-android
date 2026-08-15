package com.makeus.mody.core.camera

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

/**
 * 조정 단계: 촬영본을 세로로 드래그해 크롭 영역을 맞춘다.
 * 프레임은 화면 중앙 고정, 사진이 위아래로 움직인다. 프레임 밖은 어둡게.
 *
 * 원본은 그대로 업로드하고, 프레임 위치를 원본 대비 정규화 크롭 영역(CropRegion)으로 계산해 넘긴다.
 * 세로 슬라이스만 지원하므로 x=0, width=1, y/height 만 변한다 — 프레임은 항상 화면 폭을 채운다.
 *
 * @param frameRatio 프레임 높이/너비. 화면보다 세로로 길면 드래그 여지가 없어 1 이하만 의미가 있다.
 */
@Composable
fun AdjustLayer(
    image: UprightImage,
    frameRatio: Float,
    onRetake: () -> Unit,
    onConfirm: (originalUri: String, cropRegion: CropRegion) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val density = LocalDensity.current
        val screenH = constraints.maxHeight.toFloat()

        // 배치·크롭 계산은 CropGeometry 의 순수 함수에 있다(화면 크기·사진 비율 조합이
        // 많아 손으로 확인이 안 돼 테스트로 고정했다).
        val layout = remember(constraints, image.width, image.height, frameRatio) {
            cropLayout(
                screenWidth = constraints.maxWidth.toFloat(),
                screenHeight = screenH,
                imageWidth = image.width,
                imageHeight = image.height,
                frameRatio = frameRatio,
            )
        }
        val frameH = layout.frameHeight
        val imageTop = layout.imageTop
        val dispH = image.height * layout.imageScale
        val hasRoom = layout.hasRoom

        var frameOffset by remember { mutableFloatStateOf(0f) }
        val frameTop = layout.frameTopAt(frameOffset)
        val frameBottom = frameTop + frameH

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(image.uri, hasRoom) {
                    if (!hasRoom) return@pointerInput
                    detectDragGestures { _, drag ->
                        frameOffset =
                            (frameOffset + drag.y).coerceIn(layout.minOffset, layout.maxOffset)
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
            UploadButton(onClick = { onConfirm(image.uri, layout.cropRegionAt(frameTop)) })
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
