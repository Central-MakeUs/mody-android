package com.makeus.mody.presentation.main

import androidx.annotation.DrawableRes
import com.makeus.mody.core.designsystem.icon.ModyIcons

/**
 * 메인 하단 네비게이션 탭.
 *
 * [screenName] 은 GA4 화면 이름. 화면마다 라우트를 줄여 만든 이름(`WeeklyChallengeDetail` 등)과
 * 한 축에서 보이므로 같은 규칙(영문 PascalCase)을 쓴다 — label 은 한글이라 쓸 수 없다.
 */
enum class MainTab(
    val label: String,
    val screenName: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    // 선택 시 채운 아이콘(시안 menu 35:3564). 색은 선택 여부와 무관하게 gray10.
    FEED("피드", "Feed", ModyIcons.TabFeed, ModyIcons.TabFeedFill),
    CHALLENGE("챌린지", "Challenge", ModyIcons.TabChallenge, ModyIcons.TabChallengeFill),
    MY("마이", "MyPage", ModyIcons.TabMy, ModyIcons.TabMyFill),
}
