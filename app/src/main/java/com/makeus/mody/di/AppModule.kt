package com.makeus.mody.di

import com.makeus.mody.core.domain.notification.PushTokenSynchronizer
import com.makeus.mody.notification.PushTokenRegistrar
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * :app 계층 구현을 도메인 인터페이스에 바인딩.
 * FCM 토큰 조회는 앱(FCM SDK) 계층에 있으므로, 데이터 레이어는 [PushTokenSynchronizer] 로만 접근한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindPushTokenSynchronizer(impl: PushTokenRegistrar): PushTokenSynchronizer
}
