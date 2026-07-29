package com.makeus.mody.feature.onboarding.terms.detail.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class TermsDetailIntent : UiIntent {
    data object BackClicked : TermsDetailIntent()
}
