package com.makeus.mody.feature.auth.social

import javax.inject.Inject
import javax.inject.Singleton

/** Google Credential Manager를 통한 로그인을 담당한다. */
@Singleton
class GoogleLoginProvider @Inject constructor() {
    suspend fun login(): String {
        throw UnsupportedOperationException("Google 로그인 연동 대기: 클라이언트ID 필요")
    }
}
