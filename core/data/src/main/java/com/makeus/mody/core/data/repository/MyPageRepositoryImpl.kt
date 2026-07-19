package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.MyProfile
import com.makeus.mody.core.domain.model.WeightSummary
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.network.api.MyPageApi
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
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
}
