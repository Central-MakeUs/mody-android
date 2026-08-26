package com.makeus.mody.feature.mypage.healthrationale

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.healthrationale.contract.HealthRationaleIntent
import com.makeus.mody.feature.mypage.healthrationale.contract.HealthRationaleState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HealthRationaleViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
    private val remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<HealthRationaleState, HealthRationaleIntent>(HealthRationaleState()) {

    override suspend fun processIntent(intent: HealthRationaleIntent) {
        when (intent) {
            is HealthRationaleIntent.BackClicked ->
                navigationHelper.navigate(NavigationEvent.Up)

            is HealthRationaleIntent.PrivacyPolicyClicked -> setState {
                copy(privacyPolicyRequest = remoteConfigRepository.privacyPolicyUrl())
            }

            is HealthRationaleIntent.PrivacyPolicyLaunched ->
                setState { copy(privacyPolicyRequest = null) }
        }
    }
}
