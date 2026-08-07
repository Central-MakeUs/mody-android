package com.makeus.mody.feature.challenge.weeklydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.domain.repository.ImageShareRepository
import com.makeus.mody.core.domain.repository.ImageUploadRepository
import com.makeus.mody.core.navigation.ChallengeGraph
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.challenge.weeklydetail.contract.WeeklyChallengeDetailIntent
import com.makeus.mody.feature.challenge.weeklydetail.contract.WeeklyChallengeDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class WeeklyChallengeDetailViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val imageShareRepository: ImageShareRepository,
    private val navigationHelper: NavigationHelper,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<WeeklyChallengeDetailState, WeeklyChallengeDetailIntent>(
    savedStateHandle.toRoute<ChallengeGraph.WeeklyChallengeDetailRoute>().let { route ->
        WeeklyChallengeDetailState(
            title = route.title,
            deadlineDayOfWeek = route.deadlineDayOfWeek,
        )
    },
) {

    private val route =
        savedStateHandle.toRoute<ChallengeGraph.WeeklyChallengeDetailRoute>()

    override suspend fun processIntent(intent: WeeklyChallengeDetailIntent) {
        when (intent) {
            is WeeklyChallengeDetailIntent.ScreenEntered -> loadProofs()
            is WeeklyChallengeDetailIntent.BackClicked ->
                navigationHelper.navigate(NavigationEvent.Up)
            is WeeklyChallengeDetailIntent.AddProofClicked ->
                setState { copy(isCaptureRequested = true) }
            is WeeklyChallengeDetailIntent.CaptureLaunched ->
                setState { copy(isCaptureRequested = false) }
            is WeeklyChallengeDetailIntent.PhotoPicked -> uploadProof(intent.imageUri)
            is WeeklyChallengeDetailIntent.ShareClicked -> share()
            is WeeklyChallengeDetailIntent.ShareLaunched -> setState { copy(shareImageUri = null) }
            is WeeklyChallengeDetailIntent.ToastShown -> setState { copy(toastMessage = null) }
            is WeeklyChallengeDetailIntent.ErrorShown -> setState { copy(error = null) }
        }
    }

    /**
     * 인증 사진 목록 조회.
     *
     * 실패를 조용히 넘기면 "아직 인증한 버디가 없어요" 와 구분이 안 돼, 사용자가 남들이 안 한
     * 줄 알고 화면을 닫는다. 실패는 알리고 직전 목록은 그대로 둔다.
     */
    private fun loadProofs() = viewModelScope.launch {
        try {
            val proofs =
                challengeRepository.getWeeklyChallengeProofs(route.groupId, route.groupChallengeId)
            setState { copy(isLoading = false, proofs = proofs) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(
                    isLoading = false,
                    error = (e as? HttpResponseException)?.msg
                        ?: "인증 사진을 불러오지 못했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    /**
     * 사진 업로드 → 인증 등록 → 목록 재조회.
     *
     * 등록 응답에는 작성자 정보가 없어 그대로 목록에 끼워 넣으면 이름/아바타가 빈 칸이 된다.
     * 그래서 성공 후 목록을 다시 받는다.
     */
    private fun uploadProof(imageUri: String) = viewModelScope.launch {
        if (currentState.isUploading) return@launch
        setState { copy(isUploading = true) }
        try {
            val imageKey = imageUploadRepository.uploadImage(
                imageUri = imageUri,
                domain = PROOF_UPLOAD_DOMAIN,
                fileNameBase = "proof",
            )
            challengeRepository.createWeeklyChallengeProof(
                groupId = route.groupId,
                groupChallengeId = route.groupChallengeId,
                imageKey = imageKey,
                // 크롭 UI 가 없어 원본 그대로 올린다. 표시는 셀에서 center-crop.
                cropRegion = null,
            )
            // 여기부터의 실패는 등록 실패가 아니라 목록 갱신 실패다. 아래 catch 로 흘리면
            // "등록에 실패했어요" 가 떠서, 이미 올라간 사진을 다시 올리게 만든다.
            val proofs = try {
                challengeRepository.getWeeklyChallengeProofs(route.groupId, route.groupChallengeId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            setState {
                if (proofs != null) {
                    copy(isUploading = false, proofs = proofs, toastMessage = "인증 사진을 올렸어요!")
                } else {
                    // 성공 토스트만 띄우면 방금 올린 사진이 목록에 없는 이유를 알 수 없다.
                    copy(
                        isUploading = false,
                        error = "인증 사진은 올라갔어요. 목록을 불러오지 못했으니 다시 들어와 확인해주세요.",
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(
                    isUploading = false,
                    error = (e as? HttpResponseException)?.msg
                        ?: "인증 사진 등록에 실패했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    /** 서버가 콜라주를 만들어 주면 캐시로 내려받아 공유 시트에 넘긴다. */
    private fun share() = viewModelScope.launch {
        if (currentState.isPreparingShare) return@launch
        setState { copy(isPreparingShare = true) }
        try {
            val share = challengeRepository.shareWeeklyChallenge(
                groupId = route.groupId,
                groupChallengeId = route.groupChallengeId,
            )
            val localUri = imageShareRepository.downloadForSharing(
                imageUrl = share.imageUrl,
                fileNameBase = "weekly_challenge_${route.groupChallengeId}",
            )
            setState { copy(isPreparingShare = false, shareImageUri = localUri) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            setState {
                copy(
                    isPreparingShare = false,
                    error = (e as? HttpResponseException)?.msg
                        ?: "공유 이미지를 만들지 못했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    private companion object {
        /** presigned URL 발급 시 넘길 업로드 도메인(imageKey 접두 경로 결정). */
        // TODO(challenge): 서버와 값 확인 필요 — record/profile 외 인증 사진용 도메인.
        const val PROOF_UPLOAD_DOMAIN = "challenge"
    }
}
