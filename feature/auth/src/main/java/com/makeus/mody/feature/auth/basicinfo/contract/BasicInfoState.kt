package com.makeus.mody.feature.auth.basicinfo.contract

import com.makeus.mody.core.commonui.base.UiState

enum class BasicInfoPage { Name, Birth, Weight }

data class BasicInfoState(
    val currentPage: BasicInfoPage = BasicInfoPage.Name,
    val name: String = "",
    val birthDate: String = "",
    val currentWeight: Float? = null,
    val targetWeight: Float? = null,
) : UiState
