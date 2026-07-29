package com.makeus.mody.feature.onboarding.terms

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.OnboardingGraph
import com.makeus.mody.core.navigation.TermsType
import com.makeus.mody.feature.onboarding.terms.contract.TermsIntent
import com.makeus.mody.feature.onboarding.terms.contract.TermsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<TermsState, TermsIntent>(TermsState()) {

    override suspend fun processIntent(intent: TermsIntent) {
        when (intent) {
            TermsIntent.AllToggled -> setState {
                val next = !allChecked
                copy(privacyChecked = next, serviceChecked = next)
            }

            TermsIntent.PrivacyToggled -> setState { copy(privacyChecked = !privacyChecked) }
            TermsIntent.ServiceToggled -> setState { copy(serviceChecked = !serviceChecked) }

            TermsIntent.PrivacyDetailClicked ->
                navigationHelper.navigate(
                    NavigationEvent.To(OnboardingGraph.TermsDetailRoute(TermsType.Privacy)),
                )

            TermsIntent.ServiceDetailClicked ->
                navigationHelper.navigate(
                    NavigationEvent.To(OnboardingGraph.TermsDetailRoute(TermsType.Service)),
                )

            // 동의 완료 → 온보딩 첫 입력 스텝(닉네임)으로.
            TermsIntent.StartClicked ->
                navigationHelper.navigate(NavigationEvent.To(OnboardingGraph.NicknameRoute))
        }
    }
}
