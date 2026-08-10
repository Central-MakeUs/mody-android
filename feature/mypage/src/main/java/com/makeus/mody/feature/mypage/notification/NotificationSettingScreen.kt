package com.makeus.mody.feature.mypage.notification

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.MealExerciseSchedule
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
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

    // 알림 토글을 켤 때 OS 알림 권한(POST_NOTIFICATIONS, 13+)이 없으면 한 번 더 요청한다.
    // 서버 토글 값은 권한과 무관하게 저장하되(사용자 의도 보존), 권한이 없으면 실제 푸시가
    // 조용히 무시되므로(ModyFirebaseMessagingService) 이 지점에서 권한을 유도한다.
    val activity = context as? Activity
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // 영구 거부(다이얼로그 없이 즉시 거부 + rationale 도 false)면 시스템 알림 설정으로 안내.
        if (!granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        ) {
            context.startActivity(appNotificationSettingsIntent(context.packageName))
        }
    }
    val ensureNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    NotificationSettingContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNotificationEnabled = ensureNotificationPermission,
    )
}

/** 시스템 앱별 알림 설정 화면 인텐트(영구 거부 시 유도). API 26+ 지원. */
private fun appNotificationSettingsIntent(packageName: String): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

@Composable
private fun NotificationSettingContent(
    state: NotificationSettingState,
    onIntent: (NotificationSettingIntent) -> Unit,
    onNotificationEnabled: () -> Unit,
) {
    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "알림 설정",
                onBackClick = { onIntent(NotificationSettingIntent.BackClicked) },
            )
        },
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ModyTheme.colors.primary100)
            }
            return@ModyScreenScaffold
        }

        // 조회 실패(캐시도 없음) → 토글을 그리지 않는다. 기본값 false 3개는 "꺼짐"이 아니라
        // "아직 모름"이라, 그대로 보여주면 사용자가 켜둔 설정이 꺼진 것처럼 보이고
        // 거기서 하나만 만져도 나머지가 서버에서 실제로 꺼진다.
        if (!state.isLoaded) {
            LoadFailed(onRetry = { onIntent(NotificationSettingIntent.Load) })
            return@ModyScreenScaffold
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(8.dp))
            // 코멘트/챌린지 알림 토글: Phase 2 기능이 열렸을 때만 노출.
            // 숨겨져도 서버 PATCH(전체 교체)엔 보관된 값을 그대로 실어 설정을 보존한다.
            if (state.phaseTwoFeaturesEnabled) {
                ToggleRow(
                    title = "코멘트 알림",
                    description = "친구들이 내 기록에 남긴 댓글 알림을 받아요.",
                    checked = state.commentEnabled,
                    onCheckedChange = {
                        if (it) onNotificationEnabled()
                        onIntent(NotificationSettingIntent.CommentToggled(it))
                    },
                )
                RowDivider()
                ToggleRow(
                    title = "챌린지 알림",
                    description = "챌린지와 관련된 모든 알림을 받아요.",
                    checked = state.challengeEnabled,
                    onCheckedChange = {
                        if (it) onNotificationEnabled()
                        onIntent(NotificationSettingIntent.ChallengeToggled(it))
                    },
                )
                RowDivider()
            }
            ToggleRow(
                title = "식사 및 운동 알림",
                description = null,
                checked = state.recordReminderEnabled,
                onCheckedChange = {
                    if (it) onNotificationEnabled()
                    onIntent(NotificationSettingIntent.RecordReminderToggled(it))
                },
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
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray05,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        ModySwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** 알림 설정을 못 읽었을 때. 토글 대신 재시도만 제공한다. */
@Composable
private fun LoadFailed(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "알림 설정을 불러오지 못했어요.",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "다시 시도",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.primary100,
            modifier = Modifier
                .clickable(onClick = onRetry)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
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
