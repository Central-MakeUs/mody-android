package com.makeus.mody.core.network.di

import javax.inject.Qualifier

/**
 * presigned URL 업로드 전용 OkHttp 클라이언트.
 *
 * API 클라이언트와 **의도적으로 분리한다** — 인증 인터셉터/authenticator 와 바디 로깅이
 * 붙으면 안 되기 때문이고, 타임아웃만 다른 게 아니다. 자세한 이유는
 * [com.makeus.mody.core.network.di.NetworkModule.provideUploadOkHttpClient] 참고.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UploadClient
