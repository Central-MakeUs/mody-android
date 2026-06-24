package com.makeus.mody.feature.auth.group

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.feature.auth.group.contract.GroupIntent
import com.makeus.mody.feature.auth.group.contract.GroupState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor() :
    BaseViewModel<GroupState, GroupIntent>(GroupState()) {

    override suspend fun processIntent(intent: GroupIntent) {
        when (intent) {
            is GroupIntent.GroupCodeChanged -> setState { copy(groupCode = intent.value) }
            is GroupIntent.GroupNameChanged -> setState { copy(groupName = intent.value) }
            is GroupIntent.JoinClicked -> Unit // TODO
            is GroupIntent.CreateClicked -> Unit // TODO
        }
    }
}
