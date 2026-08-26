package com.makeus.mody.feature.mypage.healthrationale

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.mypage.healthrationale.contract.HealthRationaleIntent
import com.makeus.mody.feature.mypage.healthrationale.contract.HealthRationaleState

/**
 * 건강 데이터(Health Connect) 권한 사용 근거 화면.
 *
 * Health Connect 의 "이 앱이 데이터를 사용하는 방법" 진입점이 여기로 온다. 어떤 데이터를,
 * 무엇을 위해, 어디까지 쓰는지와 철회 방법을 한 화면에서 읽을 수 있어야 한다 —
 * 매니페스트에 인텐트 필터만 있고 이 화면이 없으면 앱 홈이 열려 확인할 방법이 없었다.
 *
 * 로그인 전에도 열릴 수 있어 세션·그룹 상태에 의존하지 않는다(정적 안내 + 방침 링크뿐).
 */
@Composable
fun HealthRationaleScreen(viewModel: HealthRationaleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.privacyPolicyRequest) {
        val url = state.privacyPolicyRequest ?: return@LaunchedEffect
        // 브라우저가 없는 기기가 있어 실패를 삼키지 않고 알린다.
        val opened = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.isSuccess
        if (!opened) {
            Toast.makeText(context, "개인정보처리방침을 열 수 없어요.", Toast.LENGTH_SHORT).show()
        }
        viewModel.onIntent(HealthRationaleIntent.PrivacyPolicyLaunched)
    }

    HealthRationaleContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun HealthRationaleContent(
    state: HealthRationaleState,
    onIntent: (HealthRationaleIntent) -> Unit,
) {
    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "건강 데이터 사용 안내",
                onBackClick = { onIntent(HealthRationaleIntent.BackClicked) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "모디는 걸음 수만 읽어요",
                style = ModyTheme.typography.h2,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "그룹 걸음 수 챌린지를 위해 필요한 최소한의 데이터만 사용합니다.",
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray06,
            )

            Spacer(modifier = Modifier.height(32.dp))
            RationaleSections.forEach { section ->
                RationaleCard(section)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        ModyButton(
            text = "개인정보처리방침 보기",
            onClick = { onIntent(HealthRationaleIntent.PrivacyPolicyClicked) },
            variant = ModyButtonVariant.Primary,
            enabled = state.privacyPolicyRequest == null,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** 한 줄 제목 + 설명 카드. 항목이 늘어도 같은 규격으로 붙는다. */
private data class RationaleSection(val title: String, val body: String)

private val RationaleSections = listOf(
    RationaleSection(
        title = "읽는 데이터",
        body = "걸음 수(Steps) 하나만 읽습니다. 심박수·수면·체중 등 다른 건강 데이터는 " +
            "요청하지도, 읽지도 않습니다.",
    ),
    RationaleSection(
        title = "사용하는 곳",
        body = "그룹 걸음 수 챌린지의 일일 목표 달성률과 버디별 기여도 순위를 계산하는 데 " +
            "씁니다. 챌린지 탭에서 바로 확인할 수 있어요.",
    ),
    RationaleSection(
        title = "저장하는 범위",
        body = "서버에는 날짜별 걸음 수 합계만 올라갑니다. 걸은 시간대나 위치 같은 " +
            "상세 기록은 보내지 않습니다.",
    ),
    RationaleSection(
        title = "제3자 제공·광고",
        body = "걸음 수를 광고에 쓰거나 다른 회사에 제공하지 않습니다.",
    ),
    RationaleSection(
        title = "언제든 끌 수 있어요",
        body = "Health Connect 설정에서 모디의 걸음 수 읽기 권한을 해제하면 즉시 중단됩니다. " +
            "권한이 없어도 기록·피드 등 나머지 기능은 그대로 쓸 수 있어요.",
    ),
)

@Composable
private fun RationaleCard(section: RationaleSection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ModyTheme.colors.gray01)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row {
            Text(
                text = section.title,
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = section.body,
            style = ModyTheme.typography.c2,
            color = ModyTheme.colors.gray06,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HealthRationaleContentPreview() {
    ModyTheme {
        HealthRationaleContent(state = HealthRationaleState(), onIntent = {})
    }
}
