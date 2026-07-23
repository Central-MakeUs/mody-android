package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MyProfile
import com.makeus.mody.core.domain.model.NotificationSettings
import com.makeus.mody.core.domain.model.ProfileDetail
import com.makeus.mody.core.domain.model.WeightSummary

/** 마이페이지 데이터. */
interface MyPageRepository {
    /** 상단 프로필(닉네임·프로필사진·함께한 일수). */
    suspend fun getProfile(): MyProfile

    /** 체중 요약(이전·현재·목표). */
    suspend fun getWeightSummary(): WeightSummary

    /** 체중 기록 생성. recordedOn: ISO(yyyy-MM-dd), weightKg: kg. */
    suspend fun recordWeight(recordedOn: String, weightKg: Double)

    /** 프로필 설정 상세(이름·생년월일·로그인 수단). */
    suspend fun getProfileDetail(): ProfileDetail

    /** 이름/생년월일 수정. */
    suspend fun updateProfile(name: String, birthDate: String?): ProfileDetail

    /**
     * 캐시된 알림 설정(로컬). 아직 캐시가 없으면 null.
     * 진입 즉시 표시용 — 서버 확인은 [refreshNotificationSettings] 로 백그라운드 재동기화한다.
     */
    suspend fun getCachedNotificationSettings(): NotificationSettings?

    /** 서버에서 알림 설정을 조회해 로컬 캐시에 반영하고 반환. */
    suspend fun refreshNotificationSettings(): NotificationSettings

    /** 알림 토글 수정(null이면 미변경). */
    suspend fun updateNotificationToggles(
        recordReminderEnabled: Boolean? = null,
        commentNotificationEnabled: Boolean? = null,
        challengeNotificationEnabled: Boolean? = null,
    )

    /** 식사(3)/운동 스케줄 수정. */
    suspend fun updateSchedules(meals: List<MealSchedule>, exercises: List<ExerciseSchedule>)
}
