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
    // 선택/미선택 동일 아이콘, 상태는 tint(gray10/gray05)로만 구분.
    FEED("피드", "Feed", ModyIcons.Feed, ModyIcons.Feed),
    CHALLENGE("챌린지", "Challenge", ModyIcons.Challenge, ModyIcons.Challenge),
    MY("마이", "MyPage", ModyIcons.Mypage, ModyIcons.Mypage),
}
