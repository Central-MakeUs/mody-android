package com.makeus.mody.core.domain.notification

/**
 * 현재 FCM 토큰을 서버에 동기화. 토큰 조회는 FCM SDK(:app) 계층에 있으므로 구현도 :app 에 둔다.
 * 데이터/도메인 레이어(로그인 성공 직후 등)는 이 인터페이스로만 동기화를 트리거한다.
 */
interface PushTokenSynchronizer {
    /** 로그인 상태면 현재 FCM 토큰을 서버에 등록/갱신. fire-and-forget(비차단). */
    fun sync()
}
