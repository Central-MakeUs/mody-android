package com.makeus.mody.feature.auth.basicinfo

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.feature.auth.basicinfo.contract.BasicInfoIntent
import com.makeus.mody.feature.auth.basicinfo.contract.BasicInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BasicInfoViewModel @Inject constructor() :
    BaseViewModel<BasicInfoState, BasicInfoIntent>(BasicInfoState()) {

    override suspend fun processIntent(intent: BasicInfoIntent) {
        when (intent) {
            is BasicInfoIntent.NameChanged -> setState { copy(name = intent.value) }
            is BasicInfoIntent.BirthChanged -> setState { copy(birthDate = intent.value) }
            is BasicInfoIntent.CurrentWeightChanged -> setState { copy(currentWeight = intent.value) }
            is BasicInfoIntent.TargetWeightChanged -> setState { copy(targetWeight = intent.value) }
            is BasicInfoIntent.NextClicked -> Unit // TODO
            is BasicInfoIntent.BackClicked -> Unit // TODO
        }
    }
}
