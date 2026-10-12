package com.makeus.mody.feature.mypage.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object MyPageGraphBaseRoute : Route

sealed interface MyPageGraph : Route {
    /** 프로필 설정(이름/생년월일/로그아웃/탈퇴) */
    @Serializable
    data object ProfileEditRoute : MyPageGraph

    /** 알림 설정(코멘트/챌린지/식사·운동 토글 + 식사/운동 스케줄) */
    @Serializable
    data object NotificationSettingRoute : MyPageGraph

    /** 그룹 설정(그룹 나가기) */
    @Serializable
    data object GroupSettingRoute : MyPageGraph

    /** 이용약관·개인정보처리방침·문의 지원 페이지(인앱 WebView) */
    @Serializable
    data object SupportRoute : MyPageGraph

    /**
     * 건강 데이터(Health Connect) 권한 설정 가이드.
     * 시스템 설정으로 바로 보내면 어디를 눌러야 할지 몰라 이탈해서, 3단계 안내를 먼저 보여준다.
     */
    @Serializable
    data object HealthGuideRoute : MyPageGraph

    /**
     * 건강 데이터(Health Connect) 권한 사용 근거 화면.
     *
     * Health Connect 앱/시스템 설정에서 "이 앱이 데이터를 사용하는 방법"을 누르면 열린다
     * (`androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` / Android 14+ 는
     * `VIEW_PERMISSION_USAGE`). 매니페스트에 인텐트 필터만 있고 이 화면이 없으면 앱 홈이
     * 열려, 어떤 데이터를 왜 쓰는지 확인할 방법이 없다 — 스토어 심사가 밟는 경로다.
     *
     * 로그인 전에도 열릴 수 있으므로 세션에 의존하지 않는다.
     */
    @Serializable
    data object HealthRationaleRoute : MyPageGraph
}
