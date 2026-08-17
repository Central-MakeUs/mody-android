package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 시안 `chip` 컴포넌트(512:4543). 상태·기한 같은 짧은 라벨.
 *
 * 공용 컴포넌트가 없어 화면마다 따로 그리다 규격이 갈라졌던 자리다. "완료" 배지와
 * 주간 챌린지 D-N 칩이 같은 시안 컴포넌트인데 padding 이 14×6 과 8×2 로 달랐고,
 * 한쪽만 시안대로였다(#135 에서 정정).
 *
 * 여기 없는 칩도 있다 — 배경·타이포·패딩이 다른 것들은 시안에서 다른 컴포넌트일 수
 * 있어 임의로 합치지 않았다(피드 "N일차", 챌린지 섹션 칩, "현재 보는 중").
 * 시안 대조 후 같은 것으로 확인되면 [ModyChipStyle] 에 추가한다.
 */
enum class ModyChipStyle {
    /** 노란 배경 + 진한 글자. 기본. */
    Primary,

    /** 어두운 배경 + 흰 글자. */
    Dark,
}

/**
 * 칩 크기. 시안이 같은 `chip` 컴포넌트를 자리마다 다른 패딩으로 쓴다 — 색과 달리
 * 하나로 못 합쳐서 실측값을 여기 모아둔다. 새 크기가 나오면 눈대중하지 말고 실측해 추가한다.
 */
enum class ModyChipSize {
    /** 시안 실측 8×2. "완료" 배지, 주간 챌린지 D-N 칩. */
    Compact,

    /** 시안 실측 12×4. 건강 데이터 연동 가이드의 STEP 배지(칩 68×28, 텍스트 44×20). */
    Large,
}

/**
 * @param text 라벨. 한 줄로 자른다 — 칩이 두 줄이 되면 옆 요소 정렬이 무너진다.
 */
@Composable
fun ModyChip(
    text: String,
    modifier: Modifier = Modifier,
    style: ModyChipStyle = ModyChipStyle.Primary,
    size: ModyChipSize = ModyChipSize.Compact,
) {
    Box(
        modifier = modifier
            .clip(ChipShape)
            .background(style.background())
            .padding(horizontal = size.horizontalPadding(), vertical = size.verticalPadding()),
    ) {
        Text(
            text = text,
            style = ModyTheme.typography.c2,
            color = style.contentColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun ModyChipSize.horizontalPadding(): Dp = when (this) {
    ModyChipSize.Compact -> 8.dp
    ModyChipSize.Large -> 12.dp
}

private fun ModyChipSize.verticalPadding(): Dp = when (this) {
    ModyChipSize.Compact -> 2.dp
    ModyChipSize.Large -> 4.dp
}

private val ChipShape = RoundedCornerShape(100.dp)

@Composable
private fun ModyChipStyle.background(): Color = when (this) {
    ModyChipStyle.Primary -> ModyTheme.colors.primary100
    ModyChipStyle.Dark -> ModyTheme.colors.gray09
}

@Composable
private fun ModyChipStyle.contentColor(): Color = when (this) {
    ModyChipStyle.Primary -> ModyTheme.colors.gray10
    ModyChipStyle.Dark -> ModyTheme.colors.white
}
