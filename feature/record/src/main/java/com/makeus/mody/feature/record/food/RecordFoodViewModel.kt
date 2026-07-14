package com.makeus.mody.feature.record.food

import com.makeus.mody.core.commonui.base.BaseViewModel
import com.makeus.mody.core.navigation.NavigationEvent
import com.makeus.mody.core.navigation.NavigationHelper
import com.makeus.mody.feature.record.food.contract.RecordFoodIntent
import com.makeus.mody.feature.record.food.contract.RecordFoodState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RecordFoodViewModel @Inject constructor(
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

            // TODO(record): 카메라 촬영/갤러리 선택 런처 연동 (권한 + PickVisualMedia)
            is RecordFoodIntent.TakePhotoClicked -> setState { copy(isPhotoSheetVisible = false) }
            is RecordFoodIntent.PickFromGalleryClicked -> setState { copy(isPhotoSheetVisible = false) }

            is RecordFoodIntent.MenuChanged -> setState { copy(menu = intent.value) }
            is RecordFoodIntent.TimeChanged ->
                setState { copy(hour24 = intent.hour24, minute = intent.minute) }

            // TODO(record): 기록 업로드 API 연동 (사진 presigned 업로드 + 기록 생성) 후 피드 복귀
            is RecordFoodIntent.SubmitClicked -> Unit
        }
    }
}
