package com.makeus.mody.feature.record.health.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class RecordHealthIntent : UiIntent {
    data object BackClicked : RecordHealthIntent()
}
