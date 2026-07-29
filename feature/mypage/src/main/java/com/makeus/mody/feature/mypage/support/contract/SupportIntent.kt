package com.makeus.mody.feature.mypage.support.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class SupportIntent : UiIntent {
    data object BackClicked : SupportIntent()
}
