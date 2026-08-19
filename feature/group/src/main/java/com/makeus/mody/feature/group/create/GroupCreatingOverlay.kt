package com.makeus.mody.feature.group.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.group.R

/**
 * 그룹 생성 API 대기 오버레이. 시안 `76:2950`.
 *
 * 공용 [com.makeus.mody.core.designsystem.component.ModyLoadingIndicator] 를 쓰지 않는다 —
 * 여기만 캐릭터 로티(`loading_group_create`, 100x100)에 안내 문구가 붙는 별도 시안이다.
 *
 * [androidx.compose.ui.window.Dialog] 가 아니라 화면 안 오버레이인 이유: 시안의 스크림이
 * 앱 콘텐츠만 덮고 키보드는 그대로 노출된다. 선례도 같은 방식이다
 * (`ProfileEditScreen` 의 저장 대기 오버레이).
 *
 * 탭을 삼켜(clickable no-op) 생성 중 입력·뒤로가기 오작동을 막는다.
 */
@Composable
internal fun GroupCreatingOverlay(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_group_create),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            // 시안 스크림: Black #000000 · opacity 0.6 (76:2949).
            .background(ModyTheme.colors.black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            // 시안 실측 260x260, 모서리 12 (76:2950 / 76:2951).
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ModyTheme.colors.white),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 시안 실측 32 (카드 상단 0 → 로티 원 32).
            Spacer(modifier = Modifier.height(32.dp))

            LottieAnimation(
                composition = composition,
                progress = { progress },
                // 시안 실측 100x100 (76:2957). 원·링·캐릭터가 모두 로티 안에 있어
                // 코드에서 원을 따로 그리지 않는다.
                modifier = Modifier.size(100.dp),
            )

            // 시안 실측 19 (로티 원 끝 132 → 제목 151).
            Spacer(modifier = Modifier.height(19.dp))

            Text(
                text = "그룹 생성 중..",
                style = ModyTheme.typography.b2,
                color = ModyTheme.colors.gray10,
                textAlign = TextAlign.Center,
            )

            // 시안 실측 8 (제목 끝 179 → 본문 187).
            Spacer(modifier = Modifier.height(8.dp))

            // 두 줄은 시안에서 별도 레이어지만 간격 19 가 C2 의 lineHeight(14 × 1.4 = 19.6)와
            // 같다. 한 Text 의 줄바꿈으로 두면 폰트 설정이 바뀌어도 어긋나지 않는다.
            Text(
                text = "잠시만 기다려주세요\n그룹을 생성하는 중이에요!",
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.gray06,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun GroupCreatingOverlayPreview() {
    ModyTheme {
        GroupCreatingOverlay()
    }
}
