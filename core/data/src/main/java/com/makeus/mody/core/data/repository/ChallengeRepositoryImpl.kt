package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.StepRecordResult
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.network.api.ChallengeApi
import com.makeus.mody.core.network.model.challenge.StepRecordUpsertRequest
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

/**
 * 서버 응답이 항상 우선이고, 실패/빈 응답일 때만 [ChallengeFallback] 으로 대체한다.
 * 폴백은 debug 소스셋에만 값이 있고 release 는 비어 있어 배포 빌드 동작은 그대로다.
 */
@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val challengeApi: ChallengeApi,
) : ChallengeRepository {

    override suspend fun getSummary(groupId: Long): ChallengeSummary =
        apiCatching {
            val s = challengeApi.getSummary(groupId).unwrapResult()
            ChallengeSummary(
                daysTogether = s.daysTogether,
                allMemberRecordedDays = s.allMemberRecordedDays,
                monthlyExerciseMinutes = s.monthlyExerciseMinutes,
                monthlyCompletedChallengeCount = s.monthlyCompletedChallengeCount,
            )
        }.getOrElse { e -> ChallengeFallback.summary ?: throw e }

    override suspend fun getNudgeTargets(groupId: Long): List<NudgeTarget> =
        apiCatching {
            challengeApi.getNudgeTargets(groupId).unwrapResult().members.map {
                NudgeTarget(
                    memberId = it.memberId,
                    nickname = it.nickname,
                    profileImageUrl = it.profileImageUrl,
                    recordedToday = it.recordedToday,
                )
            }
        }.getOrElse { e -> ChallengeFallback.nudgeTargets.ifEmpty { throw e } }
            .ifEmpty { ChallengeFallback.nudgeTargets }

    override suspend fun nudge(groupId: Long, memberId: Long) {
        apiCatching { challengeApi.nudge(groupId, memberId).unwrapResult() }
            .getOrElse { e ->
                // 더미 멤버는 서버에 없어 항상 실패하므로 폴백 데이터일 때만 성공 처리한다.
                // 실제 멤버 대상 실패까지 삼키면 디버그에서 원인을 찾을 수 없다.
                if (ChallengeFallback.nudgeTargets.none { it.memberId == memberId }) throw e
            }
    }

    override suspend fun getStepChallenge(groupId: Long): StepChallengeStatus =
        apiCatching {
            val s = challengeApi.getStepChallenge(groupId).unwrapResult()
            StepChallengeStatus(
                groupChallengeId = s.groupChallengeId,
                title = s.title,
                targetStepCount = s.targetStepCount,
                currentStepCount = s.currentStepCount,
            )
        }.getOrElse { e -> ChallengeFallback.stepChallenge ?: throw e }

    override suspend fun getStepRankings(groupId: Long): List<StepRanking> =
        apiCatching {
            challengeApi.getStepRankings(groupId).unwrapResult().rankings.map {
                StepRanking(
                    rank = it.rank,
                    memberId = it.memberId,
                    nickname = it.nickname,
                    profileImageUrl = it.profileImageUrl,
                    stepCount = it.stepCount,
                )
            }
        }.getOrElse { e -> ChallengeFallback.stepRankings.ifEmpty { throw e } }
            .ifEmpty { ChallengeFallback.stepRankings }

    override suspend fun getWeeklyChallenges(groupId: Long): List<WeeklyChallenge> =
        apiCatching {
            challengeApi.getWeeklyChallenges(groupId).unwrapResult().challenges.map {
                WeeklyChallenge(
                    groupChallengeId = it.groupChallengeId,
                    title = it.title,
                    deadlineDayOfWeek = it.deadlineDayOfWeek,
                    participantCount = it.participantCount,
                    randomParticipantNickname = it.randomParticipantNickname,
                )
            }
        }.getOrElse { e -> ChallengeFallback.weeklyChallenges.ifEmpty { throw e } }
            .ifEmpty { ChallengeFallback.weeklyChallenges }

    // 걸음 수 업로드는 폴백 대상이 아니다 — 실패를 감추면 동기화됐다고 오인한다.
    override suspend fun upsertStepRecord(
        groupId: Long,
        recordedOn: String,
        stepCount: Int,
    ): StepRecordResult {
        val r = challengeApi.upsertStepRecord(
            groupId = groupId,
            request = StepRecordUpsertRequest(recordedOn = recordedOn, stepCount = stepCount),
        ).unwrapResult()
        return StepRecordResult(
            groupChallengeId = r.groupChallengeId,
            currentStepCount = r.currentStepCount,
            targetStepCount = r.targetStepCount,
            completed = r.completed,
        )
    }
}

/** [runCatching] 과 같지만 코루틴 취소는 삼키지 않고 그대로 전파한다. */
private inline fun <T> apiCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
