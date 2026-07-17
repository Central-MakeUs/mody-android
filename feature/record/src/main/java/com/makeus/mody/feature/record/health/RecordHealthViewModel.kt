package com.makeus.mody.feature.record.health

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.repository.RecordRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.record.health.contract.ExerciseType
import com.makeus.mody.feature.record.health.contract.RecordHealthIntent
import com.makeus.mody.feature.record.health.contract.RecordHealthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordHealthViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<RecordHealthState, RecordHealthIntent>(RecordHealthState()) {

    override suspend fun processIntent(intent: RecordHealthIntent) {
        when (intent) {
            is RecordHealthIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is RecordHealthIntent.PhotoBoxClicked -> setState { copy(isPhotoSheetVisible = true) }
            is RecordHealthIntent.PhotoSheetDismissed -> setState { copy(isPhotoSheetVisible = false) }
            // 실제 카메라/갤러리 실행은 Screen 의 ActivityResult 런처가 담당, 결과는 PhotoSelected 로.
            is RecordHealthIntent.TakePhotoClicked -> setState { copy(isPhotoSheetVisible = false) }
            is RecordHealthIntent.PickFromGalleryClicked -> setState { copy(isPhotoSheetVisible = false) }
            is RecordHealthIntent.PhotoSelected -> setState { copy(photoUri = intent.uri) }

            is RecordHealthIntent.TypeDropdownToggled ->
                setState { copy(isTypeDropdownExpanded = !isTypeDropdownExpanded) }
            is RecordHealthIntent.TypeDropdownDismissed ->
                setState { copy(isTypeDropdownExpanded = false) }
            is RecordHealthIntent.TypeSelected -> selectType(intent.type)
            is RecordHealthIntent.CustomExerciseChanged ->
                setState { copy(customExercise = intent.value) }
            is RecordHealthIntent.CustomExerciseCleared ->
                setState { copy(exerciseType = null, customExercise = "", isTypeDropdownExpanded = false) }

            is RecordHealthIntent.DurationChanged ->
                setState { copy(durationHours = intent.hours, durationMinutes = intent.minutes) }

            is RecordHealthIntent.SubmitClicked -> submit()
            is RecordHealthIntent.SubmitErrorShown -> setState { copy(submitError = null) }
        }
    }

    /** 종류 선택 → 드롭다운 닫기. ETC 아니면 이전 직접입력값 비움. */
    private fun selectType(type: ExerciseType) = setState {
        copy(
            exerciseType = type,
            isTypeDropdownExpanded = false,
            customExercise = if (type == ExerciseType.ETC) customExercise else "",
        )
    }

    private fun submit() = viewModelScope.launch {
        val state = currentState
        if (!state.canSubmit || state.photoUri == null) return@launch

        setState { copy(isSubmitting = true, submitError = null) }
        try {
            recordRepository.createExerciseRecord(
                imageUri = state.photoUri,
                exerciseName = state.resolvedExerciseName,
                durationHours = state.durationHours,
                durationMinutes = state.durationMinutes,
            )
            // 기록 완료 → 피드로 복귀
            navigationHelper.navigate(NavigationEvent.Up)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 서버 message(있으면)만 노출, 그 외 기술 예외는 폴백 문구.
            val message = (e as? HttpResponseException)?.msg ?: "기록 작성에 실패했어요."
            setState { copy(isSubmitting = false, submitError = message) }
        }
    }
}
