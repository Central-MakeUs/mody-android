package com.makeus.mody.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 온보딩 전 스텝 공용 골격.
 * 상단 진행바 + 타이틀(+서브) + 가변 content + 하단 "다음으로" 버튼.
 */
@Composable
fun OnboardingScaffold(
    stepIndex: Int,
    totalSteps: Int,
    title: String,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    nextEnabled: Boolean = true,
    nextText: String = "다음으로",
    // null이면 자동: 서브타이틀 있으면 60dp, 타이틀만이면 48dp
    titleContentSpacing: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            // 입력 필드 바깥 탭 시 포커스 해제(키보드 내림)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .statusBarsPadding()
            // 키보드가 뜨면 하단 버튼이 그 위로 올라오게(ime), 안 뜨면 내비게이션바 위에
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = 24.dp),
    ) {
        StepProgressBar(
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            modifier = Modifier.padding(top = 20.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = title,
            style = ModyTheme.typography.h2,
            color = ModyTheme.colors.gray10,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray06,
            )
        }

        val contentSpacing = titleContentSpacing ?: if (subtitle != null) 60.dp else 48.dp
        Spacer(modifier = Modifier.height(contentSpacing))

        content()

        Spacer(modifier = Modifier.weight(1f))

        ModyButton(
            text = nextText,
            onClick = onNextClick,
            variant = ModyButtonVariant.Primary,
            enabled = nextEnabled,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun StepProgressBar(
    stepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSteps) { i ->
            val active = i == stepIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) ModyTheme.colors.primary100 else ModyTheme.colors.gray02
                    )
            ) {}
        }
    }
}
