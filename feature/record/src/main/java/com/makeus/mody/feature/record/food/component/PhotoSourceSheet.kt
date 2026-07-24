package com.makeus.mody.feature.record.food.component

import androidx.compose.runtime.Composable
import com.makeus.mody.core.designsystem.component.ModyPhotoSourceSheet
import com.makeus.mody.core.designsystem.component.PhotoSourceOption
import com.makeus.mody.core.designsystem.icon.ModyIcons

/** 사진 첨부 방식 선택 바텀시트: 촬영 / 갤러리. 공통 [ModyPhotoSourceSheet] 사용. */
@Composable
fun PhotoSourceSheet(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModyPhotoSourceSheet(
        options = listOf(
            PhotoSourceOption(label = "사진 촬영하기", icon = ModyIcons.Camera, onClick = onTakePhoto),
            PhotoSourceOption(label = "갤러리에서 선택하기", icon = ModyIcons.Image, onClick = onPickFromGallery),
        ),
        onDismiss = onDismiss,
    )
}
