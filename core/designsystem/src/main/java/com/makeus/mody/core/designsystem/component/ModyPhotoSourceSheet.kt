package com.makeus.mody.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** [ModyPhotoSourceSheet] 항목. 아이콘 + 라벨 + 클릭. */
data class PhotoSourceOption(
    val label: String,
    @DrawableRes val icon: Int,
    val onClick: () -> Unit,
)

/**
 * 사진 소스 선택 바텀시트(공통). 항목을 [options] 로 구성해 화면별로 다르게 쓴다.
 *  - 기록: 촬영 / 갤러리
 *  - 프로필: 갤러리 / 기본 이미지
 * 항목 사이엔 좌우 24 인셋 hairline 구분선.
 */
@Composable
fun ModyPhotoSourceSheet(
    options: List<PhotoSourceOption>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModyBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
        ) {
            options.forEachIndexed { index, option ->
                if (index > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(1.dp)
                            .background(ModyTheme.colors.gray01),
                    )
                }
                PhotoSourceRow(option)
            }
        }
    }
}

@Composable
private fun PhotoSourceRow(option: PhotoSourceOption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = option.onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(option.icon),
            contentDescription = null,
            tint = ModyTheme.colors.gray10,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = option.label,
            style = ModyTheme.typography.b4,
            color = ModyTheme.colors.gray10,
        )
    }
}
