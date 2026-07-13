package com.makeus.mody.presentation.main

import androidx.annotation.DrawableRes
import com.makeus.mody.core.designsystem.icon.ModyIcons

/** 메인 하단 네비게이션 탭. */
enum class MainTab(
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    FEED("피드", ModyIcons.Feed, ModyIcons.FeedFill),
    CHALLENGE("챌린지", ModyIcons.Award, ModyIcons.AwardFill),

    // TODO(designsystem): profile fill 에셋 미존재 → 시안 export 받으면 selectedIcon 교체.
    //  지금은 outline 아이콘에 진한 tint 로 선택 상태 표현.
    MY("마이", ModyIcons.Profile, ModyIcons.Profile),
}
