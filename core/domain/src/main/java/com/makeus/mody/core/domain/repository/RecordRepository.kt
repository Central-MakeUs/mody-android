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

    /**
     * 운동 기록 생성. [imageUri] 를 presigned 로 업로드 → 기록 생성.
     * @param imageUri content:// 또는 file:// 문자열
     * @param exerciseName 운동 종류(프리셋 라벨 또는 "기타" 직접입력)
     * @param durationHours 운동 시간(시)
     * @param durationMinutes 운동 시간(분)
     * @return 생성된 recordId
     */
    suspend fun createExerciseRecord(
        imageUri: String,
        exerciseName: String,
        durationHours: Int,
        durationMinutes: Int,
    ): Long
}
