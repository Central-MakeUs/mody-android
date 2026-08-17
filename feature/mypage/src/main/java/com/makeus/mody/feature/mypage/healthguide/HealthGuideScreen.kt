package com.makeus.mody.feature.mypage.healthguide

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyChip
import com.makeus.mody.core.designsystem.component.ModyChipSize
import com.makeus.mody.core.designsystem.component.ModyChipStyle
import com.makeus.mody.core.designsystem.component.ModyPageIndicator
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.commonui.health.openHealthConnect
import com.makeus.mody.feature.mypage.R
import com.makeus.mody.feature.mypage.healthguide.contract.HealthGuideIntent
import com.makeus.mody.feature.mypage.healthguide.contract.HealthGuideState

/**
 * 건강 데이터(Health Connect) 권한 설정 3단계 안내.
 *
 * 예전엔 "건강 데이터 연동 설정"을 누르면 곧장 시스템 설정으로 보냈는데, 그 화면에서
 * 어디를 눌러야 걸음 수를 허용하는지 알 수 없어 그냥 나가는 사람이 많았다.
 *
 * 안내 이미지는 삼성 Health Connect 기준이다. 제조사·OS 버전에 따라 실제 화면이 다를 수
 * 있지만, 눌러야 할 항목 이름(앱 권한 → 앱 액세스 → 걸음 수)은 같아 길잡이로 쓸 수 있다.
 */
@Composable
fun HealthGuideScreen(viewModel: HealthGuideViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 목적지가 기기 상태에 따라 갈려(설치됨/업데이트 필요/미설치) 인텐트는 화면이 만든다.
    LaunchedEffect(state.settingsRequest) {
        state.settingsRequest?.let {
            openHealthConnect(context, it)
            viewModel.onIntent(HealthGuideIntent.SettingsLaunched)
        }
    }

    HealthGuideContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun HealthGuideContent(
    state: HealthGuideState,
    onIntent: (HealthGuideIntent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { GuideSteps.size })

    // 페이지가 정착한 뒤에만 보고한다. 드래그 중간값까지 올리면 인디케이터가 떨린다.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { onIntent(HealthGuideIntent.StepChanged(it)) }
    }

    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "건강 데이터 연동 설정",
                onBackClick = { onIntent(HealthGuideIntent.BackClicked) },
            )
        },
    ) {
        // 시안 실측 48 (탑바 끝 110 → STEP 배지 158).
        Spacer(modifier = Modifier.height(48.dp))

        HorizontalPager(state = pagerState) { page ->
            GuideStepPage(step = GuideSteps[page])
        }

        // 시안 실측 30 (이미지 끝 628 → 인디케이터 658).
        Spacer(modifier = Modifier.height(30.dp))

        ModyPageIndicator(
            current = state.currentStep,
            total = GuideSteps.size,
            modifier = Modifier.fillMaxWidth(),
        )

        // 시안은 인디케이터 끝 666 → 버튼 786 으로 120 이지만, 화면 높이에 따라 남는 만큼
        // 벌어지는 자리다. 고정하면 작은 화면에서 버튼이 밀려난다.
        Spacer(modifier = Modifier.weight(1f))

        ModyButton(
            text = "건강 데이터 설정하러 가기",
            onClick = { onIntent(HealthGuideIntent.OpenSettingsClicked) },
            variant = ModyButtonVariant.Primary,
            // 좌우 24: 시안 버튼 x24 w354 (= 402 - 24*2).
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        // 시안 하단 여백은 40 이지만 거기엔 iPhone 홈 인디케이터(34)가 들어 있다.
        // 안드로이드는 ModyScreenScaffold 의 navigationBarsPadding 이 그 몫을 담당하므로,
        // 그 위 여백만 레포 선례(온보딩 하단 버튼)와 같은 16 으로 둔다.
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GuideStepPage(step: GuideStep) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ModyChip(
            text = step.label,
            style = ModyChipStyle.Dark,
            size = ModyChipSize.Large,
        )

        // 시안 실측 16 (배지 끝 186 → 제목 202).
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = step.title,
            style = ModyTheme.typography.b2,
            color = ModyTheme.colors.gray10,
            textAlign = TextAlign.Center,
        )

        // 시안 실측 30 (제목 끝 230 → 이미지 260).
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(step.image),
            contentDescription = null, // 옆 제목이 같은 내용을 말한다.
            // 시안 실측 282×368 (x60 → 좌우 60 중앙).
            modifier = Modifier.size(width = 282.dp, height = 368.dp),
        )
    }
}

/** 안내 한 단계. 문구는 시안 그대로. */
private data class GuideStep(
    val label: String,
    val title: String,
    @DrawableRes val image: Int,
)

private val GuideSteps = listOf(
    GuideStep("STEP 1", "‘앱 권한’을 클릭해주세요", R.drawable.img_health_guide_step1),
    GuideStep("STEP 2", "‘MODY’ 엑세스 설정으로 들어가주세요", R.drawable.img_health_guide_step2),
    GuideStep("STEP 3", "‘걸음수’를 허용해주세요", R.drawable.img_health_guide_step3),
)

@Preview(showBackground = true, heightDp = 874)
@Composable
private fun HealthGuideScreenPreview() {
    ModyTheme {
        HealthGuideContent(state = HealthGuideState(currentStep = 0), onIntent = {})
    }
}
