package com.makeus.mody.feature.mypage.profile

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditIntent
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val authRepository: AuthRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<ProfileEditState, ProfileEditIntent>(ProfileEditState()) {

    init {
        load()
    }

    override suspend fun processIntent(intent: ProfileEditIntent) {
        when (intent) {
            is ProfileEditIntent.Load -> load()
            is ProfileEditIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is ProfileEditIntent.NameChanged -> setState { copy(name = intent.value) }
            is ProfileEditIntent.SaveClicked -> save()

            is ProfileEditIntent.LogoutClicked -> logout()
            is ProfileEditIntent.WithdrawClicked -> setState { copy(showWithdrawDialog = true) }
            is ProfileEditIntent.WithdrawDismissed -> setState { copy(showWithdrawDialog = false) }
            is ProfileEditIntent.WithdrawConfirmed -> withdraw()
            is ProfileEditIntent.WithdrawCompleteConfirmed -> toAuth()

            is ProfileEditIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        try {
            val detailDeferred = async { myPageRepository.getProfileDetail() }
            val avatarDeferred = async { runCatching { myPageRepository.getProfile() }.getOrNull() }
            val detail = detailDeferred.await()
            val avatar = avatarDeferred.await()
            setState {
                copy(
                    name = detail.name,
                    originalName = detail.name,
                    birthDate = detail.birthDate,
                    loginType = detail.loginType,
                    avatarUrl = avatar?.profileImageUrl,
                    isLoading = false,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message()) }
        }
    }

    private fun save() = viewModelScope.launch {
        val state = currentState
        if (!state.isDirty || state.isSaving) return@launch
        setState { copy(isSaving = true, error = null) }
        try {
            val updated = myPageRepository.updateProfile(state.name.trim(), state.birthDate)
            setState { copy(name = updated.name, originalName = updated.name, isSaving = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isSaving = false, error = e.message()) }
        }
    }

    private fun logout() = viewModelScope.launch {
        if (currentState.isProcessing) return@launch
        setState { copy(isProcessing = true) }
        // 서버 통지 실패해도 로컬 세션은 clear → 로그인으로.
        runCatching { authRepository.logout() }
        toAuth()
    }

    private fun withdraw() = viewModelScope.launch {
        if (currentState.isProcessing) return@launch
        setState { copy(isProcessing = true, showWithdrawDialog = false) }
        try {
            authRepository.withdraw()
            // 계정은 이미 삭제됨 → isProcessing 유지해 뒤 화면 조작 차단, 완료 안내 후 이동.
            setState { copy(showWithdrawCompleteDialog = true) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState { copy(isProcessing = false, error = e.message()) }
        }
    }

    private fun toAuth() {
        navigationHelper.navigate(NavigationEvent.To(AuthGraphBaseRoute, popUpTo = true))
    }

    private fun Exception.message(): String =
        (this as? HttpResponseException)?.msg ?: "잠시 후 다시 시도해주세요."
}
