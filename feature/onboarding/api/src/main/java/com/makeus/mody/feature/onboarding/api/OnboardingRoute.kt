package com.makeus.mody.feature.onboarding.api

import com.makeus.mody.core.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraphBaseRoute : Route

/** 약관 상세로 열 문서 종류. TermsDetailRoute 인자 및 URL 매핑에 사용. */
enum class TermsType {
    /** 개인정보처리방침 */
    Privacy,

    /** 이용약관 */
    Service,
}

sealed interface OnboardingGraph : Route {
    // 입력 스텝 순서: 약관 동의 → 닉네임 → 생년월일 → 체중 → 알림 → 권한 요청 → (그룹)

    /** 로그인 직후 첫 스텝. 필수 약관 2개 동의 후 닉네임으로. */
    @Serializable
    data object TermsRoute : OnboardingGraph

    /** 약관 전문(웹) 상세. WebView 로 표시. */
    @Serializable
    data class TermsDetailRoute(val type: TermsType) : OnboardingGraph

    @Serializable
    data object NicknameRoute : OnboardingGraph

    @Serializable
    data object BirthRoute : OnboardingGraph

    @Serializable
    data object WeightRoute : OnboardingGraph

    @Serializable
    data object AlarmRoute : OnboardingGraph

    /** 접근 권한 요청(알림/카메라/사진/건강) 안내 + 요청. 프로필 저장 후 그룹 진입 직전. */
    @Serializable
    data object PermissionRoute : OnboardingGraph
}
