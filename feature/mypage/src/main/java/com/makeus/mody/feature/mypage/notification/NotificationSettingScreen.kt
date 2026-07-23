package com.makeus.mody.feature.mypage.notification

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.MealExerciseSchedule
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModySwitch
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.mypage.notification.contract.NotificationSettingIntent
import com.makeus.mody.feature.mypage.notification.contract.NotificationSettingState

@Composable
fun NotificationSettingScreen(viewModel: NotificationSettingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(NotificationSettingIntent.ErrorShown)
        }
    }

    NotificationSettingContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun NotificationSettingContent(
    state: NotificationSettingState,
    onIntent: (NotificationSettingIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        ModyBackTopBar(
            title = "알림 설정",
            onBackClick = { onIntent(NotificationSettingIntent.BackClicked) },
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ModyTheme.colors.primary100)
            }
            return@Column
        }

        Spacer(modifier = Modifier.height(8.dp))
        ToggleRow(
            title = "코멘트 알림",
            description = "친구들이 내 기록에 남긴 댓글 알림을 받아요.",
            checked = state.commentEnabled,
            onCheckedChange = { onIntent(NotificationSettingIntent.CommentToggled(it)) },
        )
        RowDivider()
        ToggleRow(
            title = "챌린지 알림",
            description = "챌린지와 관련된 모든 알림을 받아요.",
            checked = state.challengeEnabled,
            onCheckedChange = { onIntent(NotificationSettingIntent.ChallengeToggled(it)) },
        )
        RowDivider()
        ToggleRow(
            title = "식사 및 운동 알림",
            description = null,
            checked = state.recordReminderEnabled,
            onCheckedChange = { onIntent(NotificationSettingIntent.RecordReminderToggled(it)) },
        )

        if (state.recordReminderEnabled) {
            MealExerciseSchedule(
                breakfastHour = state.breakfastHour,
                lunchHour = state.lunchHour,
                dinnerHour = state.dinnerHour,
                onMealHoursChange = { b, l, d ->
                    onIntent(NotificationSettingIntent.MealHoursChanged(b, l, d))
                },
                exerciseTimes = state.exerciseTimes,
                onExerciseDaySet = { day, h, m ->
                    onIntent(NotificationSettingIntent.ExerciseDaySet(day, h, m))
                },
                onExerciseDayRemoved = { day ->
                    onIntent(NotificationSettingIntent.ExerciseDayRemoved(day))
                },
                onExerciseAllTimesSet = { h, m ->
                    onIntent(NotificationSettingIntent.ExerciseAllTimesSet(h, m))
                },
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            )
        }
    }
}

/** 제목(+설명) + 우측 스위치 행. */
@Composable
private fun ToggleRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ModyTheme.typography.b4,
                color = ModyTheme.colors.gray10,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray05,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        ModySwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun RowDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ModyTheme.colors.gray01),
    )
}
