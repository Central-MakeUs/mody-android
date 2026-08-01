package com.makeus.mody.feature.challenge.challenge

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.core.navigation.NotificationGraph
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeIntent
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<ChallengeState, ChallengeIntent>(ChallengeState()) {

    override suspend fun processIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.SubTabSelected -> setState { copy(selectedSubTab = intent.tab) }
            is ChallengeIntent.AlarmClicked ->
                navigationHelper.navigate(NavigationEvent.To(NotificationGraph.NotificationRoute))
        }
    }
}
