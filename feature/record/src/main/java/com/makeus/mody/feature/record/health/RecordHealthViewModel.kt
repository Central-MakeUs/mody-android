package com.makeus.mody.feature.record.health

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.record.health.contract.RecordHealthIntent
import com.makeus.mody.feature.record.health.contract.RecordHealthState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordHealthViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<RecordHealthState, RecordHealthIntent>(RecordHealthState()) {

    override suspend fun processIntent(intent: RecordHealthIntent) {
        when (intent) {
            is RecordHealthIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
        }
    }
}
