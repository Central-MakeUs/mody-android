package com.makeus.mody.core.data.di

import com.makeus.mody.core.data.repository.TokenManagerImpl
import com.makeus.mody.core.network.interceptor.TokenManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * :core:data 바인딩 모듈.
 * Repository 구현체 추가되면 여기 @Binds 로 인터페이스에 연결.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTokenManager(impl: TokenManagerImpl): TokenManager
}
