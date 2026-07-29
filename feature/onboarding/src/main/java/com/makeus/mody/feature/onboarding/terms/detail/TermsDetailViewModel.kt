package com.makeus.mody.feature.onboarding.terms.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.repository.RemoteConfigRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.core.navigation.TermsType
import com.makeus.mody.feature.onboarding.terms.detail.contract.TermsDetailIntent
import com.makeus.mody.feature.onboarding.terms.detail.contract.TermsDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    remoteConfigRepository: RemoteConfigRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<TermsDetailState, TermsDetailIntent>(
    initialState = run {
        val type = savedStateHandle.toRoute<OnboardingGraph.TermsDetailRoute>().type
        TermsDetailState(
            title = when (type) {
                TermsType.Privacy -> "개인정보처리방침"
                TermsType.Service -> "이용약관"
            },
            url = when (type) {
                TermsType.Privacy -> remoteConfigRepository.privacyPolicyUrl()
                TermsType.Service -> remoteConfigRepository.termsOfServiceUrl()
            },
        )
    },
) {

    override suspend fun processIntent(intent: TermsDetailIntent) {
        when (intent) {
            TermsDetailIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
        }
    }
}
