package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.theme.ModyTheme

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

/** 로딩 오버레이 딤. iOS `MLoadingModifier` 와 같은 값. */
private const val OVERLAY_DIM_ALPHA = 0.6f

/**
 * 화면 위를 어둡게 덮고 중앙에 로딩 인디케이터를 띄우는 오버레이. 아래 조작을 막는다.
 *
 * 응답을 기다리는 동작(저장·제출·확인 다이얼로그 확정 등)은 전부 이걸 쓴다 — 버튼 안 스피너나
 * 다이얼로그를 연 채 기다리는 방식은 쓰지 않는다. iOS `mLoading`(검정 60% + 같은 로티)과 같은
 * 동작이다. 확인 다이얼로그는 확정 즉시 닫고 이 오버레이를 띄운다.
 *
 * 별도 창(Dialog)으로 띄운다. 같은 화면 안에 Box 로 겹치면 터치만 막히고, TalkBack·키보드
 * 포커스로는 아래 버튼(로그아웃·탈퇴 등)에 그대로 닿는다. 창을 띄우면 아래 화면 전체가
 * 접근성·포커스·터치에서 빠지고, 하단 탭바까지 덮이며, 뒤로가기도 막힌다(iOS 와 같다).
 * 그래서 호출부는 화면 어디에 두든 상관없다.
 */
@Composable
fun ModyLoadingOverlay(
    modifier: Modifier = Modifier,
    loadingDescription: String = "로딩 중",
) {
    Dialog(
        // 대기는 사용자가 끊을 수 없다 — 끝나면 호출부가 상태를 내려 닫는다.
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        // 창 기본 딤은 끄고 아래 Box 하나로 그린다 — 두 딤이 겹치면 값이 어긋난다.
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ModyTheme.colors.black.copy(alpha = OVERLAY_DIM_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            ModyLoadingIndicator(loadingDescription = loadingDescription)
        }
    }
}
