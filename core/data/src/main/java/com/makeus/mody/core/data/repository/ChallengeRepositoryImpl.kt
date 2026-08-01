package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.network.api.ChallengeApi
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val challengeApi: ChallengeApi,
) : ChallengeRepository {

    override suspend fun getSummary(groupId: Long): ChallengeSummary {
        val s = challengeApi.getSummary(groupId).unwrapResult()
        return ChallengeSummary(
            daysTogether = s.daysTogether,
            allMemberRecordedDays = s.allMemberRecordedDays,
            monthlyExerciseMinutes = s.monthlyExerciseMinutes,
            monthlyCompletedChallengeCount = s.monthlyCompletedChallengeCount,
        )
    }

    override suspend fun getNudgeTargets(groupId: Long): List<NudgeTarget> =
        challengeApi.getNudgeTargets(groupId).unwrapResult().members.map {
            NudgeTarget(
                memberId = it.memberId,
                nickname = it.nickname,
                profileImageUrl = it.profileImageUrl,
                recordedToday = it.recordedToday,
            )
        }

    override suspend fun nudge(groupId: Long, memberId: Long) {
        challengeApi.nudge(groupId, memberId).unwrapResult()
    }

    override suspend fun getStepChallenge(groupId: Long): StepChallengeStatus {
        val s = challengeApi.getStepChallenge(groupId).unwrapResult()
        return StepChallengeStatus(
            groupChallengeId = s.groupChallengeId,
            title = s.title,
            targetStepCount = s.targetStepCount,
            currentStepCount = s.currentStepCount,
        )
    }

    override suspend fun getStepRankings(groupId: Long): List<StepRanking> =
        challengeApi.getStepRankings(groupId).unwrapResult().rankings.map {
            StepRanking(
                rank = it.rank,
                memberId = it.memberId,
                nickname = it.nickname,
                profileImageUrl = it.profileImageUrl,
                stepCount = it.stepCount,
            )
        }

    override suspend fun getWeeklyChallenges(groupId: Long): List<WeeklyChallenge> =
        challengeApi.getWeeklyChallenges(groupId).unwrapResult().challenges.map {
            WeeklyChallenge(
                groupChallengeId = it.groupChallengeId,
                title = it.title,
                deadlineDayOfWeek = it.deadlineDayOfWeek,
                participantCount = it.participantCount,
                randomParticipantNickname = it.randomParticipantNickname,
            )
        }
}
