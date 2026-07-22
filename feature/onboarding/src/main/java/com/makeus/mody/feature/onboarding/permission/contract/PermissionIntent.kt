package com.makeus.mody.feature.onboarding.permission.contract

import com.makeus.mody.core.commonui.base.UiIntent

sealed class PermissionIntent : UiIntent {
    /** "확인" — 권한 요청(선택) 처리 후 그룹으로 진입. */
    data object Continue : PermissionIntent()
}
