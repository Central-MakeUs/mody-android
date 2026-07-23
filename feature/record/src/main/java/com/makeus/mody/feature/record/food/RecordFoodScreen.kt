package com.makeus.mody.feature.record.food

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.modifier.clearFocusOnTap
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.component.ModyTimePicker
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.record.camera.RecordCameraOverlay
import com.makeus.mody.feature.record.component.SectionHeader
import com.makeus.mody.feature.record.food.component.PhotoSourceSheet
import com.makeus.mody.feature.record.food.contract.RecordFoodIntent

@Composable
fun RecordFoodScreen(viewModel: RecordFoodViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.onIntent(RecordFoodIntent.PhotoSelected(it.toString())) }
    }

    // 작성 실패 → 공용 에러 다이얼로그. 확인 시 상태 소비.
    state.submitError?.let { error ->
        ModyErrorDialog(
            title = error.title,
            message = error.message,
            onDismiss = { viewModel.onIntent(RecordFoodIntent.SubmitErrorShown) },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .clearFocusOnTap()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        ModyBackTopBar(
            title = "식사 기록",
            onBackClick = { viewModel.onIntent(RecordFoodIntent.BackClicked) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            PhotoBox(
                photoUri = state.photoUri,
                onClick = { viewModel.onIntent(RecordFoodIntent.PhotoBoxClicked) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Cook, label = "메뉴")
            Spacer(modifier = Modifier.height(12.dp))
            MenuField(
                value = state.menu,
                onValueChange = { viewModel.onIntent(RecordFoodIntent.MenuChanged(it)) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Clock, label = "식사 시간", iconSpacing = 6.dp)
            Spacer(modifier = Modifier.height(12.dp))
            ModyTimePicker(
                hour24 = state.hour24,
                minute = state.minute,
                onTimeChange = { hour24, minute ->
                    viewModel.onIntent(RecordFoodIntent.TimeChanged(hour24, minute))
                },
                selectionBarHeight = 34.dp,
                selectionBarCornerRadius = 8.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        ModyButton(
            text = "작성 완료",
            onClick = { viewModel.onIntent(RecordFoodIntent.SubmitClicked) },
            variant = ModyButtonVariant.Primary,
            enabled = state.canSubmit,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }

    if (state.isPhotoSheetVisible) {
        PhotoSourceSheet(
            onTakePhoto = { viewModel.onIntent(RecordFoodIntent.TakePhotoClicked) },
            onPickFromGallery = {
                viewModel.onIntent(RecordFoodIntent.PickFromGalleryClicked)
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDismiss = { viewModel.onIntent(RecordFoodIntent.PhotoSheetDismissed) },
        )
    }

    if (state.isCameraVisible) {
        RecordCameraOverlay(
            onConfirm = { uri -> viewModel.onIntent(RecordFoodIntent.PhotoSelected(uri)) },
            onPickGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDismiss = { viewModel.onIntent(RecordFoodIntent.CameraDismissed) },
        )
    }
}

/** 사진 영역. 미선택: 점선 업로드 박스 / 선택: 사진 채움. 탭 시 사진 소스 시트. */
@Composable
private fun PhotoBox(photoUri: String?, onClick: () -> Unit) {
    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = "선택한 식사 사진",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
        )
        return
    }

    val dashColor = ModyTheme.colors.primary100
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 1.dp.toPx() }
    val dashPx = with(density) { 6.dp.toPx() }
    val cornerPx = with(density) { 16.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ModyTheme.colors.primary400)
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(cornerPx),
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, dashPx)),
                    ),
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(ModyIcons.Image),
                contentDescription = null,
                tint = ModyTheme.colors.primary0,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "사진 업로드하기",
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.primary0,
            )
        }
    }
}

/** 박스형 메뉴 입력 필드. 포커스 시 보더가 포인트 컬러(옐로)로 바뀐다. */
@Composable
private fun MenuField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor =
        if (isFocused) ModyTheme.colors.primary100 else ModyTheme.colors.gray02

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ModyTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "메뉴를 간단하게 입력해주세요",
            textStyle = ModyTheme.typography.b4.copy(color = ModyTheme.colors.gray10),
            placeholderStyle = ModyTheme.typography.b4,
            interactionSource = interactionSource,
        )
    }
}
