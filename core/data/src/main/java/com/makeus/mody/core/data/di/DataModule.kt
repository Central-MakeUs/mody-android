package com.makeus.mody.core.data.di

import com.makeus.mody.core.data.repository.AuthRepositoryImpl
import com.makeus.mody.core.data.repository.GroupRepositoryImpl
import com.makeus.mody.core.data.repository.OnboardingRepositoryImpl
import com.makeus.mody.core.data.repository.SessionRepositoryImpl
import com.makeus.mody.core.data.repository.TokenManagerImpl
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.GroupRepository
import com.makeus.mody.core.domain.repository.OnboardingRepository
import com.makeus.mody.core.domain.repository.SessionRepository
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

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository
}
