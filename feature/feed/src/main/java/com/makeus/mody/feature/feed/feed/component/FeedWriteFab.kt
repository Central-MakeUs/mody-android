package com.makeus.mody.feature.feed.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 피드 작성 FAB + 확장 스피드다이얼 (Feed4 시안).
 * 확장 시 딤 오버레이 위에 "운동 기록"/"식사 기록" 항목 노출.
 */
@Composable
fun FeedWriteFab(
    expanded: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onWriteExercise: () -> Unit,
    onWriteMeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (expanded) {
                WriteMenuItem(label = "운동 기록", icon = ModyIcons.Exercise, onClick = onWriteExercise)
                WriteMenuItem(label = "식사 기록", icon = ModyIcons.Cook, onClick = onWriteMeal)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ModyTheme.colors.gray10)
                    .clickable(onClick = onFabClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ModyIcons.Edit),
                    contentDescription = "피드 작성",
                    tint = ModyTheme.colors.white,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun WriteMenuItem(
    label: String,
    icon: Int,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.white,
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ModyTheme.colors.white)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = ModyTheme.colors.gray10,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
