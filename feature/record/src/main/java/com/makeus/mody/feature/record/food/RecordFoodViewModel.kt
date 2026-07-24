package com.makeus.mody.feature.record.food

import androidx.lifecycle.viewModelScope
import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.domain.model.error.toErrorAlert
import com.makeus.mody.core.domain.repository.RecordRepository
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.record.food.contract.RecordFoodIntent
import com.makeus.mody.feature.record.food.contract.RecordFoodState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RecordFoodViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val navigationHelper: NavigationHelper,
) : BaseViewModel<RecordFoodState, RecordFoodIntent>(RecordFoodState()) {

    init {
        // 식사 시간 초기값 = 진입 시각 (분은 그대로)
        val now = LocalTime.now()
        setState { copy(hour24 = now.hour, minute = now.minute) }
    }

    override suspend fun processIntent(intent: RecordFoodIntent) {
        when (intent) {
            is RecordFoodIntent.BackClicked -> navigationHelper.navigate(NavigationEvent.Up)

            is RecordFoodIntent.PhotoBoxClicked -> setState { copy(isPhotoSheetVisible = true) }
            is RecordFoodIntent.PhotoSheetDismissed -> setState { copy(isPhotoSheetVisible = false) }

            // 촬영하기 → 커스텀 카메라 오버레이. 갤러리 실행은 Screen 런처, 결과는 PhotoSelected로.
            is RecordFoodIntent.TakePhotoClicked ->
                setState { copy(isPhotoSheetVisible = false, isCameraVisible = true) }
            is RecordFoodIntent.CameraDismissed -> setState { copy(isCameraVisible = false) }
            is RecordFoodIntent.PickFromGalleryClicked -> setState { copy(isPhotoSheetVisible = false) }
            is RecordFoodIntent.PhotoSelected ->
                setState { copy(photoUri = intent.uri, isCameraVisible = false) }

            is RecordFoodIntent.MenuChanged -> setState { copy(menu = intent.value) }
            is RecordFoodIntent.TimeChanged ->
                setState { copy(hour24 = intent.hour24, minute = intent.minute) }

            is RecordFoodIntent.SubmitClicked -> submit()
            is RecordFoodIntent.SubmitErrorShown -> setState { copy(submitError = null) }
        }
    }

    private fun submit() = viewModelScope.launch {
        val state = currentState
        if (!state.canSubmit || state.photoUri == null) return@launch

        setState { copy(isSubmitting = true, submitError = null) }
        try {
            recordRepository.createMealRecord(
                imageUri = state.photoUri,
                menu = state.menu.trim(),
                mealTime = LocalTime.of(state.hour24, state.minute),
            )
            // 기록 완료 → 피드로 복귀
            navigationHelper.navigate(NavigationEvent.Up)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 서버/네트워크/기타 분기는 toErrorAlert 공통 규칙을 따른다.
            setState { copy(isSubmitting = false, submitError = e.toErrorAlert("기록 작성에 실패했어요")) }
        }
    }
}
