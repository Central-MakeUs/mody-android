package com.makeus.mody.core.data.repository

import com.makeus.mody.core.data.cache.NotificationSettingsCache
import com.makeus.mody.core.data.mapper.toDomain
import com.makeus.mody.core.data.mapper.toItem
import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.LoginType
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MyProfile
import com.makeus.mody.core.domain.model.NotificationSettings
import com.makeus.mody.core.domain.model.ProfileDetail
import com.makeus.mody.core.domain.model.WeightSummary
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.network.api.MyPageApi
import com.makeus.mody.core.network.model.mypage.MyPageProfileResponse
import com.makeus.mody.core.network.model.mypage.MyPageProfileUpdateRequest
import com.makeus.mody.core.network.model.mypage.MyPageWeightCreateRequest
import com.makeus.mody.core.network.model.mypage.NotificationSettingRequest
import com.makeus.mody.core.network.model.mypage.ScheduleRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
    private val notificationCache: NotificationSettingsCache,
) : MyPageRepository {

    override suspend fun getProfile(): MyProfile {
        val me = myPageApi.getMe().unwrapResult()
        return MyProfile(
            nickname = me.nickname,
            profileImageUrl = me.profileImageUrl,
            daysTogether = me.daysTogether,
        )
    }

    override suspend fun getWeightSummary(): WeightSummary {
        val w = myPageApi.getWeights().unwrapResult()
        return WeightSummary(
            startKg = w.startWeightKg,
            currentKg = w.currentWeightKg,
            targetKg = w.targetWeightKg,
        )
    }

    override suspend fun recordWeight(recordedOn: String, weightKg: Double) {
        myPageApi.createWeight(
            MyPageWeightCreateRequest(recordedOn = recordedOn, weightKg = weightKg),
        ).unwrapResult()
    }

    override suspend fun getProfileDetail(): ProfileDetail =
        myPageApi.getProfile().unwrapResult().toDomain()

    override suspend fun updateProfile(name: String, birthDate: String?, imageKey: String?): ProfileDetail =
        myPageApi.updateProfile(
            MyPageProfileUpdateRequest(nickname = name, birthDate = birthDate, imageKey = imageKey),
        ).unwrapResult().toDomain()

    override suspend fun getCachedNotificationSettings(): NotificationSettings? =
        notificationCache.read()

    override suspend fun refreshNotificationSettings(): NotificationSettings {
        val settings = myPageApi.getNotificationSettings().unwrapResult().toDomain()
        notificationCache.write(settings)
        return settings
    }

    override suspend fun updateNotificationToggles(
        recordReminderEnabled: Boolean?,
        commentNotificationEnabled: Boolean?,
        challengeNotificationEnabled: Boolean?,
    ) {
        myPageApi.updateNotificationSettings(
            NotificationSettingRequest(
                recordReminderEnabled = recordReminderEnabled,
                commentNotificationEnabled = commentNotificationEnabled,
                challengeNotificationEnabled = challengeNotificationEnabled,
            ),
        ).unwrapResult()
        // 서버 반영 성공 → 캐시의 변경된 토글만 갱신(다음 진입 즉시성 유지). 캐시 없으면 skip.
        notificationCache.read()?.let { cached ->
            notificationCache.write(
                cached.copy(
                    recordReminderEnabled = recordReminderEnabled ?: cached.recordReminderEnabled,
                    commentNotificationEnabled = commentNotificationEnabled ?: cached.commentNotificationEnabled,
                    challengeNotificationEnabled = challengeNotificationEnabled ?: cached.challengeNotificationEnabled,
                ),
            )
        }
    }

    override suspend fun updateSchedules(
        meals: List<MealSchedule>,
        exercises: List<ExerciseSchedule>,
    ) {
        myPageApi.updateSchedules(
            ScheduleRequest(
                mealSchedules = meals.map { it.toItem() },
                exerciseSchedules = exercises.map { it.toItem() },
            ),
        ).unwrapResult()
        // 서버 반영 성공 → 캐시 스케줄 교체. 캐시 없으면 skip.
        notificationCache.read()?.let { cached ->
            notificationCache.write(cached.copy(meals = meals, exercises = exercises))
        }
    }

    private fun MyPageProfileResponse.toDomain(): ProfileDetail = ProfileDetail(
        name = name,
        birthDate = birthDate,
        loginType = LoginType.from(loginType),
    )
}
