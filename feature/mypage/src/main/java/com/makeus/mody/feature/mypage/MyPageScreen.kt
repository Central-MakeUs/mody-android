package com.makeus.mody.feature.mypage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyLogoTopBar
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.HealthAvailability
import com.makeus.mody.core.domain.model.WeightSummary
import com.makeus.mody.feature.mypage.contract.MyPageIntent
import com.makeus.mody.feature.mypage.contract.MyPageState
import com.makeus.mody.feature.mypage.weight.WeightRecordSheet
import kotlin.math.roundToInt

@Composable
fun MyPageScreen(viewModel: MyPageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // 탭 진입 시 최신화(다른 화면에서 프로필/체중 변경 반영). VM은 탭 전환에도 유지되므로 재조회 필요.
    LaunchedEffect(Unit) { viewModel.onIntent(MyPageIntent.Refresh) }
    // 체중 저장 실패 → 토스트 1회(시트는 유지되어 재시도 가능).
    LaunchedEffect(state.weightError) {
        state.weightError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(MyPageIntent.WeightErrorShown)
        }
    }
    // 건강 데이터 연동 설정 → Health Connect(시스템). 앱 내 화면 없이 그쪽으로 넘긴다.
    LaunchedEffect(state.healthSettingsRequest) {
        state.healthSettingsRequest?.let {
            openHealthConnect(context, it)
            viewModel.onIntent(MyPageIntent.HealthSettingsLaunched)
        }
    }
    MyPageContent(state = state, onIntent = viewModel::onIntent)
}

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

/**
 * 건강 데이터 연동 설정 진입.
 * - 사용 가능: 우리 앱의 Health Connect 권한 화면 → (액션 미지원 기기면) Health Connect 홈.
 * - 미설치/업데이트 필요: 플레이스토어의 Health Connect 페이지 → (스토어 앱 없으면) 웹.
 *
 * 앞의 것부터 시도하고 열리는 첫 인텐트를 쓴다. 전부 실패하는 기기가 있어 토스트로 마무리.
 */
private fun openHealthConnect(context: Context, availability: HealthAvailability) {
    val candidates = when (availability) {
        HealthAvailability.AVAILABLE -> listOf(
            // Android 14+ 는 플랫폼이, 그 이하는 Health Connect 앱이 처리하는 별개 액션.
            Intent(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    "android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"
                } else {
                    "androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS"
                },
            ).putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName),
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS),
        )

        HealthAvailability.UPDATE_REQUIRED, HealthAvailability.UNAVAILABLE -> listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"),
            ),
        )
    }
    val launched = candidates.any { intent ->
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .isSuccess
    }
    if (!launched) {
        Toast.makeText(context, "건강 데이터 설정을 열 수 없어요.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun MyPageContent(
    state: MyPageState,
    onIntent: (MyPageIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .verticalScroll(rememberScrollState()),
    ) {
        ModyLogoTopBar(
            onAlarmClick = { onIntent(MyPageIntent.AlarmClicked) },
            hasUnreadNotification = state.hasUnreadNotification,
        )

        Spacer(modifier = Modifier.height(28.dp))
        ProfileRow(
            nickname = state.nickname,
            avatarUrl = state.profileImageUrl,
            daysTogether = state.daysTogether,
            onProfileSettingClick = { onIntent(MyPageIntent.ProfileSettingClicked) },
        )

        Spacer(modifier = Modifier.height(24.dp))
        WeightSection(
            weight = state.weight,
            onRecordClick = { onIntent(MyPageIntent.WeightRecordClicked) },
        )

        Spacer(modifier = Modifier.height(8.dp))
        SectionDivider()

        SettingsRow("알림 설정") { onIntent(MyPageIntent.NotificationSettingClicked) }
        SettingsRow("그룹 설정") { onIntent(MyPageIntent.GroupSettingClicked) }
        // 건강 데이터 연동(걸음 수 챌린지용): Phase 2 기능이 열렸을 때만 노출.
        if (state.phaseTwoFeaturesEnabled) {
            SettingsRow("건강 데이터 연동 설정") { onIntent(MyPageIntent.HealthDataSettingClicked) }
        }

        // 스토어 정책 필수: 이용약관·개인정보처리방침·문의를 모은 지원 페이지(인앱 WebView).
        SettingsRow("문의 및 약관확인") { onIntent(MyPageIntent.SupportClicked) }
    }

    if (state.showWeightSheet) {
        WeightRecordSheet(
            // 휠 기본값 = 현재 체중(없으면 60kg).
            initialWeightKg = state.weight?.currentKg?.roundToInt() ?: 60,
            isSaving = state.isRecordingWeight,
            onConfirm = { recordedOn, weightKg ->
                onIntent(MyPageIntent.WeightRecordSubmitted(recordedOn, weightKg))
            },
            onDismiss = { onIntent(MyPageIntent.WeightRecordDismissed) },
        )
    }
}

@Composable
private fun ProfileRow(
    nickname: String,
    avatarUrl: String?,
    daysTogether: Int,
    onProfileSettingClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyAvatar(imageUrl = avatarUrl, size = 48.dp)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nickname,
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.height(2.dp))
            val dateStyle = ModyTheme.typography.b5
            Text(
                text = buildAnnotatedString {
                    append("모디와 함께한지 ")
                    withStyle(
                        SpanStyle(
                            color = ModyTheme.colors.primary100,
                            fontSize = dateStyle.fontSize,
                            fontWeight = dateStyle.fontWeight,
                        ),
                    ) { append("${daysTogether}일") }
                    append("째")
                },
                // 기본(모디와 함께한지 / 째) = b7 · gray05
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray05,
            )
        }
        // 프로필 설정 칩
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ModyTheme.colors.gray03)
                .clickable(onClick = onProfileSettingClick)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Text(
                text = "프로필 설정",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.white,
            )
        }
    }
}

@Composable
private fun WeightSection(
    weight: WeightSummary?,
    onRecordClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "체중 기록",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ModyTheme.colors.gray01)
                .padding(12.dp),
        ) {
            WeightCard(weight = weight)
            Spacer(modifier = Modifier.height(16.dp))
            ModyButton(
                text = "체중 기록하기",
                onClick = onRecordClick,
                variant = ModyButtonVariant.Dark,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 이전 > 현재 > 목표 3열. 값 없으면 "-". */
@Composable
private fun WeightCard(weight: WeightSummary?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.white)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeightColumn(label = "이전 체중", kg = weight?.startKg, modifier = Modifier.weight(1f))
        WeightArrow()
        WeightColumn(label = "현재 체중", kg = weight?.currentKg, modifier = Modifier.weight(1f))
        WeightArrow()
        WeightColumn(label = "목표 체중", kg = weight?.targetKg, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WeightColumn(label: String, kg: Double?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, style = ModyTheme.typography.c2, color = ModyTheme.colors.gray05)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = kg?.let { formatKg(it) } ?: "-",
                style = ModyTheme.typography.h2,
                color = ModyTheme.colors.gray10,
            )
            Text(
                text = " kg",
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray08,
            )
        }
    }
}

@Composable
private fun WeightArrow() {
    Icon(
        painter = painterResource(ModyIcons.Right),
        contentDescription = null,
        tint = ModyTheme.colors.gray02,
        modifier = Modifier.size(24.dp),
    )
}

/** 56.0 → "56", 56.5 → "56.5". */
private fun formatKg(kg: Double): String =
    if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(ModyTheme.colors.gray01),
    )
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ModyTheme.typography.b4,
                color = ModyTheme.colors.gray09,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(ModyIcons.Right),
                contentDescription = null,
                tint = ModyTheme.colors.gray04,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray01),
        )
    }
}
