package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.SplashGate
import kotlinx.coroutines.flow.StateFlow

/**
 * 원격 기능 플래그(Firebase Remote Config).
 * 아직 공개하지 않을 기능을 서버에서 켜고 끌 수 있게 한다.
 */
interface RemoteConfigRepository {
    /**
     * Phase 2 기능(챌린지 탭·콕 찌르기·관련 알림 토글) 노출 여부.
     * is_phase_one_flag 의 부정 — 기본 false(전부 숨김, 심사 안전).
     *
     * 응원 댓글은 여기 얹지 않는다. 원격이 아니라 코드에서 껐다 —
     * feature:feed 의 FeedState.commentEnabled 참고.
     */
    val phaseTwoFeaturesEnabled: StateFlow<Boolean>

    /**
     * 스토어 심사용 히든(게스트) 로그인 허용 여부 — guest_login_flag.
     * 기본 false(차단). 심사 기간에만 콘솔에서 PRD true 로 열어둔다.
     */
    val guestLoginEnabled: StateFlow<Boolean>

    /**
     * 심사용 히든 로그인 비밀번호 — review_login_password.
     *
     * APK 상수로 두면 디컴파일로 그대로 노출되고 교체에 재배포가 필요해 원격에서만 받는다.
     * 기본값은 빈 문자열이며, **빈 값이면 히든 로그인을 열지 않는다**(빈 입력 통과 방지).
     * 심사 기간에 [guestLoginEnabled] 와 함께 콘솔에서 설정해야 동작한다.
     */
    fun reviewLoginPassword(): String

    /** 개인정보처리방침 웹 URL(약관 상세 WebView). 기본값 = GitHub Pages. */
    fun privacyPolicyUrl(): String

    /** 이용약관 웹 URL(약관 상세 WebView). 기본값 = GitHub Pages. */
    fun termsOfServiceUrl(): String

    /** 원격 값 fetch & activate. 실패해도 마지막 활성값/기본값 유지. */
    suspend fun refresh()

    /** 스플래시 게이트 구성값 스냅샷. [refresh] 이후 호출해야 최신 활성값 기준. */
    fun splashGate(): SplashGate
}
