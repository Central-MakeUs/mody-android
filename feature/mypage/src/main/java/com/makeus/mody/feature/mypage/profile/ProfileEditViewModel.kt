package com.makeus.mody.feature.mypage.profile

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.AuthRepository
import com.makeus.mody.core.domain.repository.ImageUploadRepository
import com.makeus.mody.core.domain.model.LoginType
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.domain.repository.SessionRepository
import com.makeus.mody.core.navigation.AuthGraphBaseRoute
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditIntent
import com.makeus.mody.feature.mypage.profile.contract.ProfileEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

private const val PROFILE_DOMAIN = "profile"
private const val PROFILE_FILE_BASE = "profile"

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<ProfileEditState, ProfileEditIntent>(ProfileEditState()) {

    init {
        load()
    }

    override suspend fun processIntent(intent: ProfileEditIntent) {
        when (intent) {
            is ProfileEditIntent.Load -> load()
            // 저장 안 된 변경이 있으면 확인 다이얼로그, 없으면 바로 나감.
            is ProfileEditIntent.BackClicked ->
                if (currentState.isDirty) setState { copy(showLeaveDialog = true) }
                else navigationHelper.navigate(NavigationEvent.Up)

            // 저장(업로드 포함) 중 편집 차단 — submitProfile 완료 시점의 상태 덮어쓰기로
            // 저장 도중 입력한 변경이 유실되는 것을 막는다.
            is ProfileEditIntent.NameChanged ->
                if (!currentState.isSaving && !currentState.isDemo) setState { copy(name = intent.value) }
            is ProfileEditIntent.SaveClicked -> save()

            // 데모 세션은 프로필 편집 불가(저장 API 가 생년월일 등 소셜 가입 정보를 요구).
            is ProfileEditIntent.AvatarClicked ->
                if (!currentState.isSaving && !currentState.isDemo) setState { copy(isPhotoSheetVisible = true) }
            is ProfileEditIntent.PhotoSheetDismissed -> setState { copy(isPhotoSheetVisible = false) }
            is ProfileEditIntent.GalleryImageSelected ->
                if (!currentState.isSaving) {
                    setState { copy(pendingImageUri = intent.uri, pendingResetDefault = false, isPhotoSheetVisible = false) }
                }
            is ProfileEditIntent.UseDefaultImageClicked ->
                if (!currentState.isSaving) {
                    setState { copy(pendingResetDefault = true, pendingImageUri = null, isPhotoSheetVisible = false) }
                }

            is ProfileEditIntent.LeaveSaveClicked -> saveAndLeave()
            is ProfileEditIntent.LeaveDiscardClicked -> {
                setState { copy(showLeaveDialog = false) }
                navigationHelper.navigate(NavigationEvent.Up)
            }
            is ProfileEditIntent.LeaveDismissed -> setState { copy(showLeaveDialog = false) }

            is ProfileEditIntent.LogoutClicked -> logout()
            is ProfileEditIntent.WithdrawClicked -> setState { copy(showWithdrawDialog = true) }
            is ProfileEditIntent.WithdrawDismissed -> setState { copy(showWithdrawDialog = false) }
            is ProfileEditIntent.WithdrawConfirmed -> withdraw()
            is ProfileEditIntent.WithdrawCompleteConfirmed -> toAuth()

            is ProfileEditIntent.ErrorShown -> setState { copy(error = null, errorTitle = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        // 심사용 데모 세션: 소셜 계정이 없어 프로필 상세(/mypage/profile)가 항상 실패(MYPAGE302)
        // → 호출 자체를 생략하고 정상 동작하는 /mypage/me 값만으로 읽기 전용 표시.
        if (sessionRepository.isGuestLogin()) {
            val me = runCatching { myPageRepository.getProfile() }.getOrNull()
            setState {
                copy(
                    name = me?.nickname ?: "데모 계정",
                    originalName = me?.nickname ?: "데모 계정",
                    avatarUrl = me?.profileImageUrl,
                    loginType = LoginType.DEMO,
                    isDemo = true,
                    isLoading = false,
                )
            }
            return@launch
        }
        // async 를 launch 자식으로 두고 던지면 try/catch 를 우회해 부모로 전파(크래시).
        // 각 호출을 null 로 흡수한 뒤 supervisorScope 로 병렬 실행한다.
        val (detail, avatar) = supervisorScope {
            val detailDeferred = async { detailOrNull() }
            val avatarDeferred = async { runCatching { myPageRepository.getProfile() }.getOrNull() }
            detailDeferred.await() to avatarDeferred.await()
        }
        if (detail == null) {
            setState { copy(isLoading = false, error = "프로필을 불러오지 못했어요.") }
            return@launch
        }
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
    }

    /** 취소는 전파, 그 외 실패만 null. */
    private suspend fun detailOrNull() = try {
        myPageRepository.getProfileDetail()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private fun save() = viewModelScope.launch {
        if (!currentState.isDirty || currentState.isSaving) return@launch
        submitProfile()
    }

    /** 다이얼로그에서 "저장 후 나가기". 저장 성공 시 이전 화면으로, 실패 시 머무르며 에러. */
    private fun saveAndLeave() = viewModelScope.launch {
        if (currentState.isSaving) return@launch // 이중 제출 방지
        setState { copy(showLeaveDialog = false) }
        if (!currentState.isDirty) {
            navigationHelper.navigate(NavigationEvent.Up)
            return@launch
        }
        if (submitProfile()) navigationHelper.navigate(NavigationEvent.Up)
    }

    /**
     * 이름/생년월일 + (변경 시)프로필 이미지 저장.
     * 갤러리 선택이면 먼저 업로드해 imageKey 를 얻고, 기본 리셋이면 ""(서버 기본 처리)로 보낸다.
     * @return 저장 성공 여부.
     */
    private suspend fun submitProfile(): Boolean {
        val state = currentState
        val trimmed = state.name.trim()
        setState { copy(isSaving = true, error = null) }
        return try {
            val imageKey = when {
                state.pendingImageUri != null ->
                    imageUploadRepository.uploadImage(state.pendingImageUri, PROFILE_DOMAIN, PROFILE_FILE_BASE)
                state.pendingResetDefault -> "" // 기본 이미지 리셋
                else -> null // 이미지 변경 없음
            }
            val updatedImageUrl = myPageRepository.updateProfile(trimmed, state.birthDate, imageKey)
            // PATCH 응답의 반영된 profileImageUrl 을 그대로 사용(별도 /me 재조회 불필요).
            //  - 이미지 미변경: 기존 값 유지
            //  - 리셋: 서버가 준 기본 URL(없으면 null → 기본 아바타)
            //  - 갤러리: 서버 URL, 비어 오면 방금 고른 로컬 Uri 로 폴백
            val newAvatar = when {
                !state.isAvatarChanged -> state.avatarUrl
                state.pendingResetDefault -> updatedImageUrl
                else -> updatedImageUrl ?: state.pendingImageUri
            }
            setState {
                copy(
                    name = trimmed,
                    originalName = trimmed,
                    avatarUrl = newAvatar,
                    pendingImageUri = null,
                    pendingResetDefault = false,
                    isSaving = false,
                )
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 저장(이미지 업로드 포함) 실패 → 전용 다이얼로그 문구.
            setState {
                copy(
                    isSaving = false,
                    errorTitle = "프로필을 저장하지 못했어요",
                    error = "다시 시도해주세요",
                )
            }
            false
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
