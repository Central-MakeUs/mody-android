package com.makeus.mody.feature.auth.di

import com.makeus.mody.core.domain.repository.SocialLoginProvider
import com.makeus.mody.feature.auth.social.SocialLoginProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindSocialLoginProvider(impl: SocialLoginProviderImpl): SocialLoginProvider
}
