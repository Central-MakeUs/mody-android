package com.makeus.mody.presentation.main

import androidx.annotation.DrawableRes
import com.makeus.mody.core.designsystem.icon.ModyIcons

/** 메인 하단 네비게이션 탭. */
enum class MainTab(
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    // 선택/미선택 동일 아이콘, 상태는 tint(gray10/gray05)로만 구분.
    FEED("피드", ModyIcons.Feed, ModyIcons.Feed),
    CHALLENGE("챌린지", ModyIcons.Challenge, ModyIcons.Challenge),
    MY("마이", ModyIcons.Mypage, ModyIcons.Mypage),
}
