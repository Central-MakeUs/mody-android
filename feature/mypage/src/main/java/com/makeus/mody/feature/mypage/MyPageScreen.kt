package com.makeus.mody.feature.mypage

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.commonui.health.openHealthConnect
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyAvatarSkeleton
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyLogoTopBar
import com.makeus.mody.core.designsystem.component.ModyTextSkeleton
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
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

private val ProfileAvatarSize = 48.dp

/** 스켈레톤 폭 — 들어갈 값의 대략적인 글자 수를 가늠한 값. */
private val NicknameSkeletonWidth = 88.dp
private val DaysTogetherSkeletonWidth = 148.dp
private val WeightSkeletonWidth = 56.dp

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
            isLoaded = state.isProfileLoaded,
            onProfileSettingClick = { onIntent(MyPageIntent.ProfileSettingClicked) },
        )

        Spacer(modifier = Modifier.height(24.dp))
        WeightSection(
            weight = state.weight,
            isLoaded = state.isWeightLoaded,
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
    isLoaded: Boolean,
    onProfileSettingClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 조회 전 ModyAvatar 를 그리면 기본 아바타가 떴다가 실제 사진으로 바뀐다.
        if (isLoaded) {
            ModyAvatar(imageUrl = avatarUrl, size = ProfileAvatarSize)
        } else {
            ModyAvatarSkeleton(size = ProfileAvatarSize)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (isLoaded) {
                Text(
                    text = nickname,
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                )
            } else {
                ModyTextSkeleton(
                    width = NicknameSkeletonWidth,
                    lineHeight = ModyTheme.typography.b3.lineHeight,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val dateStyle = ModyTheme.typography.b5
            if (isLoaded) {
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
            } else {
                // 문구까지 통째로 가린다 — 일수만 스켈레톤이면 "모디와 함께한지 [   ]째" 가 돼
                // 값이 비어 보이는 것과 다를 게 없다.
                ModyTextSkeleton(
                    width = DaysTogetherSkeletonWidth,
                    lineHeight = ModyTheme.typography.b7.lineHeight,
                )
            }
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
    isLoaded: Boolean,
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
            WeightCard(weight = weight, isLoaded = isLoaded)
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

/** 이전 > 현재 > 목표 3열. 조회 전엔 스켈레톤, 조회 후 값이 없으면 "-". */
@Composable
private fun WeightCard(weight: WeightSummary?, isLoaded: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.white)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeightColumn("이전 체중", weight?.startKg, isLoaded, Modifier.weight(1f))
        WeightArrow()
        WeightColumn("현재 체중", weight?.currentKg, isLoaded, Modifier.weight(1f))
        WeightArrow()
        WeightColumn("목표 체중", weight?.targetKg, isLoaded, Modifier.weight(1f))
    }
}

@Composable
private fun WeightColumn(
    label: String,
    kg: Double?,
    isLoaded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 라벨은 고정 문구라 그대로 둔다 — 숫자 자리만 가린다.
        Text(text = label, style = ModyTheme.typography.c2, color = ModyTheme.colors.gray05)
        Spacer(modifier = Modifier.height(4.dp))
        if (!isLoaded) {
            ModyTextSkeleton(
                width = WeightSkeletonWidth,
                lineHeight = ModyTheme.typography.h2.lineHeight,
            )
            return@Column
        }
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
