package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.makeus.mody.core.designsystem.R

/**
 * 공용 로딩 인디케이터. API 응답을 기다리는 모든 화면에서 동일하게 사용.
 *
 * 시안 로티(`loading_mody`, 50x50 · 30fps · 2초 루프)를 무한 재생한다.
 * 예전엔 [androidx.compose.material3.CircularProgressIndicator] 였고 색상만 받았는데,
 * 로티는 색이 애니메이션 안에 들어 있어 색 인자가 의미가 없어졌다.
 *
 * @param size 재생 크기. 기본값은 원본 규격과 같은 50dp.
 *             호출부에서 [Modifier.size] 를 거는 대신 이 인자를 쓴다 — modifier 로 걸면
 *             내부 size 와 겹쳐 어느 쪽이 이기는지가 체인 순서에 따라 달라진다.
 * @param loadingDescription 스크린리더가 읽을 문구. 무엇을 기다리는지 알릴 수 있으면
 *                           화면별로 덮어쓴다(예: "그룹을 만드는 중").
 */
@Composable
fun ModyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    loadingDescription: String = "로딩 중",
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_mody),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        // 로티 프레임 자체는 읽을 게 없어 자식 시맨틱은 지우되, "진행 중"이라는 사실은 남긴다.
        // 교체 전 CircularProgressIndicator 가 기본으로 주던 정보라, 안 넣으면 스크린리더
        // 사용자에게 대기 중임이 전달되지 않는 퇴행이 된다.
        modifier = modifier
            .size(size)
            .clearAndSetSemantics {
                contentDescription = loadingDescription
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            },
    )
}

/**
 * 화면 전체를 채우고 중앙에 로딩 인디케이터를 띄우는 래퍼.
 * 최초 로드 대기(값 준비 전) 시 콘텐츠 대신 표시.
 */
@Composable
fun ModyLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ModyLoadingIndicator()
    }
}
