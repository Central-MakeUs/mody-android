package com.makeus.mody.core.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 원격 기능 플래그(Firebase Remote Config).
 * 아직 공개하지 않을 기능을 서버에서 켜고 끌 수 있게 한다.
 */
interface RemoteConfigRepository {
    /** 챌린지 탭 노출 여부. 기본 false(숨김) → 서버에서 true 로 켜면 노출. */
    val challengeEnabled: StateFlow<Boolean>

    /** 원격 값 fetch & activate. 실패해도 마지막 활성값/기본값 유지. */
    suspend fun refresh()
}
