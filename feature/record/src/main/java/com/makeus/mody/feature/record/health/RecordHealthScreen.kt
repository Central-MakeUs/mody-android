package com.makeus.mody.feature.record.health

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyDurationPicker
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.record.camera.RecordCameraOverlay
import com.makeus.mody.feature.record.component.RecordPhotoBox
import com.makeus.mody.feature.record.component.SectionHeader
import com.makeus.mody.feature.record.food.component.PhotoSourceSheet
import com.makeus.mody.feature.record.health.contract.ExerciseType
import com.makeus.mody.feature.record.health.contract.RecordHealthIntent

@Composable
fun RecordHealthScreen(viewModel: RecordHealthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.onIntent(RecordHealthIntent.PhotoSelected(it.toString())) }
    }

    // 작성 실패 → 토스트 1회 후 소비
    LaunchedEffect(state.submitError) {
        state.submitError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(RecordHealthIntent.SubmitErrorShown)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        ModyBackTopBar(
            title = "운동 기록",
            onBackClick = { viewModel.onIntent(RecordHealthIntent.BackClicked) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            RecordPhotoBox(
                photoUri = state.photoUri,
                contentDescription = "선택한 운동 사진",
                onClick = { viewModel.onIntent(RecordHealthIntent.PhotoBoxClicked) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Exercise, label = "운동 종류")
            Spacer(modifier = Modifier.height(12.dp))
            ExerciseTypeField(
                selectedType = state.exerciseType,
                isCustom = state.isCustomType,
                customValue = state.customExercise,
                expanded = state.isTypeDropdownExpanded,
                onFieldClick = { viewModel.onIntent(RecordHealthIntent.TypeDropdownToggled) },
                onOptionClick = { viewModel.onIntent(RecordHealthIntent.TypeSelected(it)) },
                onCustomChange = { viewModel.onIntent(RecordHealthIntent.CustomExerciseChanged(it)) },
                onClear = { viewModel.onIntent(RecordHealthIntent.CustomExerciseCleared) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(icon = ModyIcons.Clock, label = "운동 시간", iconSpacing = 6.dp)
            Spacer(modifier = Modifier.height(12.dp))
            ModyDurationPicker(
                hours = state.durationHours,
                minutes = state.durationMinutes,
                onChange = { hours, minutes ->
                    viewModel.onIntent(RecordHealthIntent.DurationChanged(hours, minutes))
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        ModyButton(
            text = "작성 완료",
            onClick = { viewModel.onIntent(RecordHealthIntent.SubmitClicked) },
            variant = ModyButtonVariant.Primary,
            enabled = state.canSubmit,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }

    if (state.isPhotoSheetVisible) {
        PhotoSourceSheet(
            onTakePhoto = { viewModel.onIntent(RecordHealthIntent.TakePhotoClicked) },
            onPickFromGallery = {
                viewModel.onIntent(RecordHealthIntent.PickFromGalleryClicked)
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDismiss = { viewModel.onIntent(RecordHealthIntent.PhotoSheetDismissed) },
        )
    }

    if (state.isCameraVisible) {
        RecordCameraOverlay(
            onConfirm = { uri -> viewModel.onIntent(RecordHealthIntent.PhotoSelected(uri)) },
            onPickGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDismiss = { viewModel.onIntent(RecordHealthIntent.CameraDismissed) },
        )
    }
}

/**
 * 운동 종류 선택 필드.
 * - 미선택/프리셋: 드롭다운 셀렉트(탭 시 옵션 펼침, 선택값 표시).
 * - "기타": 직접입력 텍스트필드로 전환(포커스 시 보더 옐로).
 */
@Composable
private fun ExerciseTypeField(
    selectedType: ExerciseType?,
    isCustom: Boolean,
    customValue: String,
    expanded: Boolean,
    onFieldClick: () -> Unit,
    onOptionClick: (ExerciseType) -> Unit,
    onCustomChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    if (isCustom) {
        CustomExerciseField(
            value = customValue,
            onValueChange = onCustomChange,
            onClear = onClear,
        )
        return
    }

    val borderColor = if (expanded) ModyTheme.colors.primary100 else ModyTheme.colors.gray02
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(onClick = onFieldClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedType?.label ?: "운동 선택",
                style = ModyTheme.typography.b4,
                color = if (selectedType == null) ModyTheme.colors.gray05 else ModyTheme.colors.gray10,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(ModyIcons.Down),
                contentDescription = null,
                tint = ModyTheme.colors.gray05,
                modifier = Modifier.size(20.dp),
            )
        }

        if (expanded) {
            // 시안: 흰 bg + 그림자 + 아래 모서리만 라운드. 아이템 상하 12dp(연속 배치 → 텍스트간 24dp).
            val listShape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = listShape)
                    .clip(listShape)
                    .background(ModyTheme.colors.white),
            ) {
                ExerciseType.entries.forEach { type ->
                    val selected = type == selectedType
                    Text(
                        text = type.label,
                        style = ModyTheme.typography.b4,
                        color = ModyTheme.colors.gray10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionClick(type) }
                            .background(
                                if (selected) ModyTheme.colors.primary400 else ModyTheme.colors.white,
                            )
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

/**
 * "기타" 직접입력 필드. 포커스 시 보더가 포인트 컬러(옐로).
 * 우측 X → [onClear] 로 종류 미선택 상태 복귀(다시 선택 가능).
 */
@Composable
private fun CustomExerciseField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) ModyTheme.colors.primary100 else ModyTheme.colors.gray02

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ModyTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "운동 종류를 입력해주세요",
            textStyle = ModyTheme.typography.b4.copy(color = ModyTheme.colors.gray10),
            placeholderStyle = ModyTheme.typography.b4,
            trailingIcon = ModyIcons.Clear,
            onTrailingIconClick = onClear,
            trailingIconContentDescription = "운동 종류 다시 선택",
            interactionSource = interactionSource,
        )
    }
}
