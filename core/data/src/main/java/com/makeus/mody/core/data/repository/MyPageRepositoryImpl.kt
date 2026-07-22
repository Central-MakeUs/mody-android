package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.LoginType
import com.makeus.mody.core.domain.model.MyProfile
import com.makeus.mody.core.domain.model.ProfileDetail
import com.makeus.mody.core.domain.model.WeightSummary
import com.makeus.mody.core.domain.repository.MyPageRepository
import com.makeus.mody.core.network.api.MyPageApi
import com.makeus.mody.core.network.model.mypage.MyPageProfileResponse
import com.makeus.mody.core.network.model.mypage.MyPageProfileUpdateRequest
import com.makeus.mody.core.network.model.mypage.MyPageWeightCreateRequest
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

    override suspend fun recordWeight(recordedOn: String, weightKg: Double) {
        myPageApi.createWeight(
            MyPageWeightCreateRequest(recordedOn = recordedOn, weightKg = weightKg),
        ).unwrapResult()
    }

    override suspend fun getProfileDetail(): ProfileDetail =
        myPageApi.getProfile().unwrapResult().toDomain()

    override suspend fun updateProfile(name: String, birthDate: String?): ProfileDetail =
        myPageApi.updateProfile(
            MyPageProfileUpdateRequest(nickname = name, birthDate = birthDate),
        ).unwrapResult().toDomain()

    private fun MyPageProfileResponse.toDomain(): ProfileDetail = ProfileDetail(
        name = name,
        birthDate = birthDate,
        loginType = LoginType.from(loginType),
    )
}
