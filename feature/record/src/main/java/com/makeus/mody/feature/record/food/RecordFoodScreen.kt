package com.makeus.mody.feature.record.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackButton
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.record.food.component.MealTimePicker
import com.makeus.mody.feature.record.food.component.PhotoSourceSheet
import com.makeus.mody.feature.record.food.contract.RecordFoodIntent

@Composable
fun RecordFoodScreen(viewModel: RecordFoodViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        RecordFoodTopBar(onBackClick = { viewModel.onIntent(RecordFoodIntent.BackClicked) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            PhotoUploadBox(onClick = { viewModel.onIntent(RecordFoodIntent.PhotoBoxClicked) })

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Cook, label = "메뉴")
            Spacer(modifier = Modifier.height(12.dp))
            MenuField(
                value = state.menu,
                onValueChange = { viewModel.onIntent(RecordFoodIntent.MenuChanged(it)) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Clock, label = "식사 시간")
            Spacer(modifier = Modifier.height(8.dp))
            MealTimePicker(
                hour24 = state.hour24,
                minute = state.minute,
                onTimeChange = { hour24, minute ->
                    viewModel.onIntent(RecordFoodIntent.TimeChanged(hour24, minute))
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.isPhotoSheetVisible) {
        PhotoSourceSheet(
            onTakePhoto = { viewModel.onIntent(RecordFoodIntent.TakePhotoClicked) },
            onPickFromGallery = { viewModel.onIntent(RecordFoodIntent.PickFromGalleryClicked) },
            onDismiss = { viewModel.onIntent(RecordFoodIntent.PhotoSheetDismissed) },
        )
    }
}

@Composable
private fun RecordFoodTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyBackButton(onClick = onBackClick)
        Text(
            text = "식사 기록",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
        )
    }
}

/** 점선 테두리 사진 업로드 박스. 클릭 시 사진 소스 선택 시트. */
@Composable
private fun PhotoUploadBox(onClick: () -> Unit) {
    val dashColor = ModyTheme.colors.primary100
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 1.5.dp.toPx() }
    val dashPx = with(density) { 6.dp.toPx() }
    val cornerPx = with(density) { 12.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
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
                tint = ModyTheme.colors.primary100,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "사진 업로드하기",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.primary100,
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ModyTheme.colors.gray10,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray10,
        )
    }
}

/** 박스형 메뉴 입력 필드 (시안: 라운드 보더 + 내부 패딩). */
@Composable
private fun MenuField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ModyTheme.colors.gray02,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        ModyTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "메뉴를 간단하게 입력해주세요",
            textStyle = ModyTheme.typography.b7.copy(color = ModyTheme.colors.gray10),
            placeholderStyle = ModyTheme.typography.b7,
        )
    }
}
