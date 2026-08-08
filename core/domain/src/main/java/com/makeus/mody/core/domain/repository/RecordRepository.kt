package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.CropRegion
import java.time.LocalTime

/** 기록 생성(식사/운동). 사진은 원본을 presigned 업로드하고 imageKey + 크롭 영역으로 참조된다. */
interface RecordRepository {

    /**
     * 식사 기록 생성. [imageUri](원본)를 presigned 로 업로드 → 기록 생성.
     * @param imageUri content:// 또는 file:// 문자열
     * @param cropRegion 표시용 크롭 영역(정규화). 갤러리 선택 등 크롭 없으면 null.
     * @return 생성된 recordId
     */
    suspend fun createMealRecord(
        imageUri: String,
        menu: String,
        mealTime: LocalTime,
        cropRegion: CropRegion?,
    ): Long

    /**
     * 운동 기록 생성. [imageUri](원본)를 presigned 로 업로드 → 기록 생성.
     * @param imageUri content:// 또는 file:// 문자열
     * @param exerciseName 운동 종류(프리셋 라벨 또는 "기타" 직접입력)
     * @param durationHours 운동 시간(시)
     * @param durationMinutes 운동 시간(분)
     * @param cropRegion 표시용 크롭 영역(정규화). 갤러리 선택 등 크롭 없으면 null.
     * @return 생성된 recordId
     */
    suspend fun createExerciseRecord(
        imageUri: String,
        exerciseName: String,
        durationHours: Int,
        durationMinutes: Int,
        cropRegion: CropRegion?,
    ): Long

    /**
     * 기록 삭제. 기록 단위라 그룹을 받지 않는다 — 여러 그룹에 올라간 기록이면 전부에서 사라진다.
     * 남의 기록인지는 서버가 판단하므로, 화면은 내 기록에만 메뉴를 노출하는 정도로 충분하다.
     */
    suspend fun deleteRecord(recordId: Long)
}
