package com.makeus.mody.feature.record.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyBackButton
import com.makeus.mody.core.designsystem.theme.ModyTheme

/** 기록 화면 공용 탑바: 뒤로가기 + 타이틀 (시안: 높이 48, 타이틀 b6/gray09). */
@Composable
fun RecordTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyBackButton(onClick = onBackClick)
        Text(
            text = title,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray09,
        )
    }
}
