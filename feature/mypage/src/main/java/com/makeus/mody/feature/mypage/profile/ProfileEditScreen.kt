package com.makeus.mody.feature.mypage.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyInputFilter
import com.makeus.mody.core.designsystem.component.ModyLoadingScreen
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.modifier.clearFocusOnTap
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.LoginType
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditIntent
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditState

/** 저장 액션 색(시안 지정). */
private val SaveBlue = Color(0xFF6E7CFF)

@Composable
fun ProfileEditScreen(viewModel: ProfileEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(ProfileEditIntent.ErrorShown)
        }
    }

    ProfileEditContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun ProfileEditContent(
    state: ProfileEditState,
    onIntent: (ProfileEditIntent) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    // 시스템 back도 가로채 변경사항 확인. (변경 없으면 비활성 → 기본 pop)
    BackHandler(enabled = state.isDirty) { onIntent(ProfileEditIntent.BackClicked) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .clearFocusOnTap()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBar(
            showSave = state.isDirty && !state.isSaving,
            onBack = { onIntent(ProfileEditIntent.BackClicked) },
            onSave = {
                // 저장 시 포커스 해제 → 키보드 내림 + X/글자수 숨김.
                focusManager.clearFocus()
                onIntent(ProfileEditIntent.SaveClicked)
            },
        )

        // 최초 로드 전에는 값(loginType 등)이 UNKNOWN이라 배지/필드가 깜빡임 → 준비될 때까지 인디케이터.
        if (state.isLoading) {
            ModyLoadingScreen()
            return@Column
        }

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

    if (state.showLeaveDialog) {
        ModyDialog(
            title = "변경사항이 있습니다",
            message = "저장하지 않고 나가시겠어요?",
            confirmText = "저장",
            onConfirm = { onIntent(ProfileEditIntent.LeaveSaveClicked) },
            // 왼쪽 버튼(저장 안 함) = 그냥 나가기, 스크림/백키 = 취소(머무름).
            dismissText = "저장 안 함",
            onDismiss = { onIntent(ProfileEditIntent.LeaveDiscardClicked) },
            onDismissRequest = { onIntent(ProfileEditIntent.LeaveDismissed) },
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
    // 필드 높이(52)만 레이아웃에 반영. 경고/카운트 Row는 offset으로 아래에 겹쳐 그려
    // 나타나도 아래 콘텐츠(생년월일 등)를 밀지 않음(추가 패딩 없음).
    Box(modifier = Modifier.fillMaxWidth().height(52.dp)) {
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
                alertIcon = if (focused && isOverLimit) R.drawable.ic_alert_filled else null,
                // X(지우기)는 포커스 상태에서만 노출.
                trailingIcon = if (focused && value.isNotEmpty()) R.drawable.ic_clear else null,
                onTrailingIconClick = { onValueChange("") },
                trailingIconContentDescription = "입력 지우기",
                // 한 글자 더(초과) 입력돼야 경고가 뜨도록 최대 길이는 NAME_MAX + 1.
                maxLength = ProfileEditState.NAME_MAX + 1,
                inputFilter = ModyInputFilter::hangulAlphaNumeric,
            )
        }

        // 경고 문구 + 글자수 카운트는 포커스 상태에서만 표시. 필드 아래 6dp 지점에 오버레이.
        if (focused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 필드(높이 52) 바로 아래 6dp 지점. 오버레이라 아래 콘텐츠엔 영향 없음.
                    .offset(y = 58.dp),
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

/**
 * 로그인 수단 배지(읽기 전용). 로그인 페이지 버튼(ModyButton)을 그대로 재사용 —
 * 아이콘/색상 동일, 문구만 "…계정으로 로그인 중". UNKNOWN이면 미표시.
 * onClick은 no-op(읽기 전용).
 */
@Composable
private fun LoginBadge(loginType: LoginType) {
    val (variant, icon, label) = when (loginType) {
        LoginType.KAKAO ->
            Triple(ModyButtonVariant.Kakao, ModyIcons.Kakao, "카카오 계정으로 로그인 중")
        LoginType.GOOGLE ->
            Triple(ModyButtonVariant.Google, ModyIcons.Google, "Google 계정으로 로그인 중")
        LoginType.UNKNOWN -> return
    }
    ModyButton(
        text = label,
        onClick = {},
        variant = variant,
        leadingIcon = icon,
    )
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

