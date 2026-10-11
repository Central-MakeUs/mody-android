package com.makeus.mody.feature.mypage.healthguide

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.repository.HealthRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.healthguide.contract.HealthGuideIntent
import com.makeus.mody.feature.mypage.healthguide.contract.HealthGuideState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HealthGuideViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<HealthGuideState, HealthGuideIntent>(HealthGuideState()) {

    override suspend fun processIntent(intent: HealthGuideIntent) {
        when (intent) {
            is HealthGuideIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is HealthGuideIntent.StepChanged -> setState { copy(currentStep = intent.step) }

            // 버튼을 누른 시점의 기기 상태를 다시 읽는다. 가이드를 보는 동안 사용자가
            // Health Connect 를 설치하거나 업데이트했을 수 있어, 진입 때 읽은 값은 낡는다.
            is HealthGuideIntent.OpenSettingsClicked ->
                setState { copy(settingsRequest = healthRepository.availability()) }

            is HealthGuideIntent.SettingsLaunched -> setState { copy(settingsRequest = null) }
        }
    }
}
