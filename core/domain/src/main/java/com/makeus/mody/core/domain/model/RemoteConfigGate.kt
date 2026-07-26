package com.makeus.mody.core.domain.model

/**
 * 스플래시 진입 게이트 구성값. iOS 와 공용 Firebase Remote Config 파라미터에서 읽는다.
 * 검사 순서(iOS 와 동일): force_update_flag → minimum_supported_version → notice_flag.
 */
data class SplashGate(
    /** 긴급 강제 업데이트 스위치. true 면 버전 무관 업데이트 요구. */
    val forceUpdate: Boolean,
    /** 최소 지원 버전("1.2.3"). 현재 버전이 미만이면 업데이트 요구. null/blank 면 검사 생략. */
    val minimumSupportedVersion: String?,
    /** 스토어 이동 URL. 없으면 호출 측에서 마켓 스킴으로 폴백. */
    val appStoreUrl: String?,
    /** 공지. null 이면 공지 없음. */
    val notice: RemoteNotice?,
)

/** 원격 공지(notice_flag JSON). */
data class RemoteNotice(
    val title: String,
    val message: String,
    /** true: 확인 후 진행 가능(안내용) / false: 확인 비활성 + 진행 차단. */
    val skipPossible: Boolean,
)
