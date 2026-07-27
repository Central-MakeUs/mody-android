package com.makeus.mody.feature.onboarding.terms

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.onboarding.terms.contract.TermsIntent
import com.makeus.mody.feature.onboarding.terms.contract.TermsState

@Composable
fun TermsScreen(viewModel: TermsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TermsContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun TermsContent(
    state: TermsState,
    onIntent: (TermsIntent) -> Unit,
) {
    ModyScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "필수 약관에 동의해주세요",
                style = ModyTheme.typography.h2,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MODY를 시작하려면 약관 동의가 필요해요.",
                style = ModyTheme.typography.b4,
                color = ModyTheme.colors.gray05,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 전체 동의(강조 박스)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.primary100.copy(alpha = 0.12f))
                    .clickable { onIntent(TermsIntent.AllToggled) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckIcon(checked = state.allChecked)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "전체 동의",
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            RequiredTermsRow(
                label = "개인정보처리방침 (필수)",
                checked = state.privacyChecked,
                onCheckClick = { onIntent(TermsIntent.PrivacyToggled) },
                onDetailClick = { onIntent(TermsIntent.PrivacyDetailClicked) },
            )
            RequiredTermsRow(
                label = "이용약관 (필수)",
                checked = state.serviceChecked,
                onCheckClick = { onIntent(TermsIntent.ServiceToggled) },
                onDetailClick = { onIntent(TermsIntent.ServiceDetailClicked) },
            )
        }

        ModyButton(
            text = "시작하기",
            onClick = { onIntent(TermsIntent.StartClicked) },
            enabled = state.canStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

/** 필수 약관 한 행: 좌측 체크(토글) + 라벨/셰브론(전문 보기). */
@Composable
private fun RequiredTermsRow(
    label: String,
    checked: Boolean,
    onCheckClick: () -> Unit,
    onDetailClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 체크는 동의 토글, 라벨/셰브론은 전문 보기로 분리.
        CheckIcon(
            checked = checked,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onCheckClick() }
                .padding(4.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = ModyTheme.typography.b4,
            color = ModyTheme.colors.gray08,
            modifier = Modifier
                .weight(1f)
                .clickable { onDetailClick() },
        )
        Icon(
            painter = painterResource(R.drawable.ic_right),
            contentDescription = "전문 보기",
            tint = ModyTheme.colors.gray04,
            modifier = Modifier
                .size(20.dp)
                .clickable { onDetailClick() },
        )
    }
}

@Composable
private fun CheckIcon(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(R.drawable.ic_check),
        contentDescription = null,
        tint = if (checked) ModyTheme.colors.primary100 else ModyTheme.colors.gray04,
        modifier = modifier.size(20.dp),
    )
}
