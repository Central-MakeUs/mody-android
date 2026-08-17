package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 페이지 인디케이터. 현재 페이지는 길쭉한 알약, 나머지는 점.
 *
 * 시안 실측: 활성 20×8, 점 8×8, 간격 8. 시안 안의 인디케이터 7곳(기록 상세 `chat`,
 * 건강 데이터 연동 가이드 등)이 전부 같은 규격이라 공용으로 둔다 — 화면마다 따로 그리면
 * 반드시 갈라진다. 실제로 기록 상세가 16×8·간격 6 으로 어긋나 있었다.
 *
 * 비활성 점 색은 시안에서 변수에 바인딩돼 있지 않아(활성만 `Main`) 실측하지 못했다.
 * 기존 기록 상세 구현이 쓰던 gray02 를 그대로 따른다 — 시안이 토큰을 붙이면 그때 맞춘다.
 */
@Composable
fun ModyPageIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 1) return
    Row(
        // 장식이라 스크린리더가 점 개수를 읽어봐야 도움이 안 된다. 페이지 정보는 본문이 말한다.
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(IndicatorGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .height(DotSize)
                    .width(if (active) ActiveWidth else DotSize)
                    .clip(CircleShape)
                    .background(if (active) ModyTheme.colors.primary100 else ModyTheme.colors.gray02),
            )
        }
    }
}

/** 시안 실측 20 (활성 알약 폭). */
private val ActiveWidth = 20.dp

/** 시안 실측 8 (점 지름 = 인디케이터 높이). */
private val DotSize = 8.dp

/** 시안 실측 8 (알약 끝 20 → 다음 점 28). */
private val IndicatorGap = 8.dp
