package com.makeus.mody.feature.mypage.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyInputFilter
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.LoginType
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditIntent
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditState

/** 저장 액션 색(시안 지정). */
private val SaveBlue = Color(0xFF6E7CFF)

@Composable
fun ProfileEditScreen(viewModel: ProfileEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileEditContent(state = state, onIntent = viewModel::onIntent)

    // 실패(조회/저장/탈퇴) → 공용 에러 다이얼로그. 확인 시 상태 소비.
    ModyErrorDialog(
        message = state.error,
        onDismiss = { viewModel.onIntent(ProfileEditIntent.ErrorShown) },
    )
}

@Composable
private fun ProfileEditContent(
    state: ProfileEditState,
    onIntent: (ProfileEditIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBar(
            showSave = state.isDirty && !state.isSaving,
            onBack = { onIntent(ProfileEditIntent.BackClicked) },
            onSave = { onIntent(ProfileEditIntent.SaveClicked) },
        )

        Spacer(modifier = Modifier.height(24.dp))
        ModyAvatar(
            imageUrl = state.avatarUrl,
            size = 80.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            FieldLabel("이름")
            EditableNameField(
                value = state.name,
                isOverLimit = state.isNameOverLimit,
                onValueChange = { onIntent(ProfileEditIntent.NameChanged(it)) },
            )

            Spacer(modifier = Modifier.height(20.dp))
            FieldLabel("생년월일")
            ReadOnlyField(text = state.birthDateDisplay)

            Spacer(modifier = Modifier.height(20.dp))
            LoginBadge(loginType = state.loginType)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()

        SettingRow(text = "로그아웃", color = ModyTheme.colors.gray09) {
            if (!state.isProcessing) onIntent(ProfileEditIntent.LogoutClicked)
        }
        SettingRow(text = "탈퇴하기", color = ModyTheme.colors.error) {
            if (!state.isProcessing) onIntent(ProfileEditIntent.WithdrawClicked)
        }
    }

    if (state.showWithdrawDialog) {
        ModyDialog(
            title = "정말 모디를 떠나실건가요?",
            message = "탈퇴하면 계정 내 모든 정보가 사라져요.",
            confirmText = "계정 삭제",
            onConfirm = { onIntent(ProfileEditIntent.WithdrawConfirmed) },
            dismissText = "취소",
            onDismissRequest = { onIntent(ProfileEditIntent.WithdrawDismissed) },
        )
    }

    if (state.showWithdrawCompleteDialog) {
        // 계정은 이미 삭제된 상태 → 스크림/백키로 닫아도 동일하게 로그인으로 이동.
        ModyDialog(
            title = "탈퇴 처리가 완료되었어요.",
            message = "마음이 바뀐다면 꼭 다시 찾아와주세요!",
            confirmText = "확인",
            onConfirm = { onIntent(ProfileEditIntent.WithdrawCompleteConfirmed) },
            onDismissRequest = { onIntent(ProfileEditIntent.WithdrawCompleteConfirmed) },
        )
    }
}

@Composable
private fun TopBar(showSave: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = "뒤로가기",
                tint = ModyTheme.colors.gray10,
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "프로필 설정",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
            modifier = Modifier.weight(1f),
        )
        if (showSave) {
            Text(
                text = "저장",
                style = ModyTheme.typography.b6,
                color = SaveBlue,
                modifier = Modifier.clickable(onClick = onSave),
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = ModyTheme.typography.b7,
        color = ModyTheme.colors.gray08,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * 이름 입력 필드. 온보딩 닉네임과 동일 동작:
 *  - X(지우기) 아이콘, 초과 시 경고 아이콘 + 빨간 보더 + "N자 이내로 입력해주세요" + 글자수 카운트.
 */
@Composable
private fun EditableNameField(
    value: String,
    isOverLimit: Boolean,
    onValueChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        isOverLimit -> ModyTheme.colors.error
        focused -> ModyTheme.colors.primary100
        else -> ModyTheme.colors.gray02
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ModyTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "이름을 입력해주세요",
            interactionSource = interactionSource,
            alertIcon = if (isOverLimit) R.drawable.ic_alert_filled else null,
            trailingIcon = if (value.isNotEmpty()) R.drawable.ic_clear else null,
            onTrailingIconClick = { onValueChange("") },
            trailingIconContentDescription = "입력 지우기",
            // 한 글자 더(초과) 입력돼야 경고가 뜨도록 최대 길이는 NAME_MAX + 1.
            maxLength = ProfileEditState.NAME_MAX + 1,
            inputFilter = ModyInputFilter::hangulAlphaNumeric,
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 온보딩 닉네임/그룹명과 동일: 에러문구·카운트를 필드 안쪽 8dp 지점(화면 기준 32dp)에.
            .padding(start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (isOverLimit) "${ProfileEditState.NAME_MAX}자 이내로 입력해주세요" else "",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.error,
        )
        Text(
            text = buildAnnotatedString {
                val count = value.length.toString()
                append(count)
                append("/${ProfileEditState.NAME_MAX}")
                if (isOverLimit) addStyle(SpanStyle(color = ModyTheme.colors.error), 0, count.length)
            },
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray07,
        )
    }
}

/** 읽기 전용(생년월일). 회색 텍스트. */
@Composable
private fun ReadOnlyField(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, ModyTheme.colors.gray02, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            style = ModyTheme.typography.b4,
            color = ModyTheme.colors.gray04,
        )
    }
}

/** 로그인 수단 배지(읽기 전용). 카카오=옐로, 그 외=회색. */
@Composable
private fun LoginBadge(loginType: LoginType) {
    val (bg, label, textColor) = when (loginType) {
        LoginType.KAKAO -> Triple(Color(0xFFFEE500), "카카오 계정으로 로그인 중", ModyTheme.colors.gray10)
        LoginType.GOOGLE -> Triple(ModyTheme.colors.gray01, "구글 계정으로 로그인 중", ModyTheme.colors.gray10)
        LoginType.UNKNOWN -> Triple(ModyTheme.colors.gray01, "로그인 중", ModyTheme.colors.gray10)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = ModyTheme.typography.b6, color = textColor)
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(ModyTheme.colors.gray01),
    )
}

@Composable
private fun SettingRow(text: String, color: Color, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = text, style = ModyTheme.typography.b4, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModyTheme.colors.gray01),
        )
    }
}

