package com.makeus.mody.core.domain.repository

import java.time.LocalTime

/** 기록 생성(식사/운동). 사진은 presigned 업로드 후 imageKey 로 참조된다. */
interface RecordRepository {

    /**
     * 식사 기록 생성. [imageUri] 를 presigned 로 업로드 → 기록 생성.
     * @param imageUri content:// 또는 file:// 문자열
     * @return 생성된 recordId
     */
    suspend fun createMealRecord(
        imageUri: String,
        menu: String,
        mealTime: LocalTime,
    ): Long
}
