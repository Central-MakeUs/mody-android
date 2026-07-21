package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.MyProfile
import com.makeus.mody.core.domain.model.ProfileDetail
import com.makeus.mody.core.domain.model.WeightSummary

/** 마이페이지 데이터. */
interface MyPageRepository {
    /** 상단 프로필(닉네임·프로필사진·함께한 일수). */
    suspend fun getProfile(): MyProfile

    /** 체중 요약(이전·현재·목표). */
    suspend fun getWeightSummary(): WeightSummary

    /** 프로필 설정 상세(이름·생년월일·로그인 수단). */
    suspend fun getProfileDetail(): ProfileDetail

    /** 이름/생년월일 수정. */
    suspend fun updateProfile(name: String, birthDate: String?): ProfileDetail
}
