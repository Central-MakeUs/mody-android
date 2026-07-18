package com.makeus.mody.core.domain.repository

/**
 * FCM 디바이스 토큰을 서버에 등록/해제.
 * deviceId·platform 은 구현체(데이터 레이어)가 채운다 → 호출부는 fcmToken 만 안다.
 */
interface PushTokenRepository {
    /** 현재 기기의 fcmToken 을 서버에 등록/갱신. */
    suspend fun register(fcmToken: String)

    /** 로그아웃 등에서 이 기기 토큰을 서버에서 비활성. */
    suspend fun unregister()
}
