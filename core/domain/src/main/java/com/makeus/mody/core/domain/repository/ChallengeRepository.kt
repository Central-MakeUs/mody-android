package com.makeus.mody.core.domain.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeOption
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.StepRecordResult
import com.makeus.mody.core.domain.model.WeeklyChallenge

/** 챌린지 탭 — 그룹 요약 통계, 버디 신기록(콕 찌르기). */
interface ChallengeRepository {
    /** 연속 기록 탭 상단 요약(전원 연속 기록 일수·D+N·운동시간·챌린지 개수). */
    suspend fun getSummary(groupId: Long): ChallengeSummary

    /** 버디 신기록 도전 목록(멤버별 오늘 기록 여부). */
    suspend fun getNudgeTargets(groupId: Long): List<NudgeTarget>

    /** 오늘 기록 안 한 버디 콕 찌르기(푸시 발송). */
    suspend fun nudge(groupId: Long, memberId: Long)

    /** 진행 중인 그룹 필수(걸음 수) 챌린지 현황. */
    suspend fun getStepChallenge(groupId: Long): StepChallengeStatus

    /** 챌린지 변경 화면에 띄울 선택 가능한 걸음 수 챌린지 목록. */
    suspend fun getStepChallengeOptions(groupId: Long): List<StepChallengeOption>

    /**
     * 진행 중인 걸음 수 챌린지를 [challengeId] 로 교체한다.
     * 서버에서 기존 걸음 기록이 초기화되므로 호출 전 사용자 확인이 필요하다.
     */
    suspend fun changeStepChallenge(groupId: Long, challengeId: Long): StepChallengeStatus

    /** 걸음 수 기여도 순위. */
    suspend fun getStepRankings(groupId: Long): List<StepRanking>

    /** 이번주 그룹 선택(주간) 챌린지 목록. */
    suspend fun getWeeklyChallenges(groupId: Long): List<WeeklyChallenge>

    /**
     * 오늘 걸음 수를 서버에 반영(upsert). 같은 날짜로 다시 보내면 덮어쓴다.
     * @param recordedOn 기록 날짜(yyyy-MM-dd)
     */
    suspend fun upsertStepRecord(
        groupId: Long,
        recordedOn: String,
        stepCount: Int,
    ): StepRecordResult
}
