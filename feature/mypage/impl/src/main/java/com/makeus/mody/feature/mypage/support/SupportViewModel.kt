package com.makeus.mody.feature.mypage.support

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.support.contract.SupportIntent
import com.makeus.mody.feature.mypage.support.contract.SupportState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<SupportState, SupportIntent>(SupportState()) {

    override suspend fun processIntent(intent: SupportIntent) {
        when (intent) {
            SupportIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)
        }
    }
}
