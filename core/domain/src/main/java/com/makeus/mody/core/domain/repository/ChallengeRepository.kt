package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget

/** 챌린지 탭 — 그룹 요약 통계, 버디 신기록(콕 찌르기). */
interface ChallengeRepository {
    /** 연속 기록 탭 상단 요약(전원 연속 기록 일수·D+N·운동시간·챌린지 개수). */
    suspend fun getSummary(groupId: Long): ChallengeSummary

    /** 버디 신기록 도전 목록(멤버별 오늘 기록 여부). */
    suspend fun getNudgeTargets(groupId: Long): List<NudgeTarget>

    /** 오늘 기록 안 한 버디 콕 찌르기(푸시 발송). */
    suspend fun nudge(groupId: Long, memberId: Long)
}
