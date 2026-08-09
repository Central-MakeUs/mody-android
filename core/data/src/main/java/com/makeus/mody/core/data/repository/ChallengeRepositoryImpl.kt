package com.makeus.mody.core.data.repository

import com.makeus.mody.core.domain.model.ChallengeSummary
import com.makeus.mody.core.domain.model.CropRegion
import com.makeus.mody.core.domain.model.NudgeButtonStatus
import com.makeus.mody.core.domain.model.NudgeTarget
import com.makeus.mody.core.domain.model.StepChallengeOption
import com.makeus.mody.core.domain.model.StepChallengeStatus
import com.makeus.mody.core.domain.model.StepRanking
import com.makeus.mody.core.domain.model.StepRecordResult
import com.makeus.mody.core.domain.model.WeeklyChallenge
import com.makeus.mody.core.domain.model.WeeklyChallengeParticipant
import com.makeus.mody.core.domain.model.WeeklyChallengeProof
import com.makeus.mody.core.domain.model.WeeklyChallengeShare
import com.makeus.mody.core.domain.repository.ChallengeRepository
import com.makeus.mody.core.network.api.ChallengeApi
import com.makeus.mody.core.network.model.challenge.StepChallengeChangeRequest
import com.makeus.mody.core.network.model.challenge.StepRecordUpsertRequest
import com.makeus.mody.core.network.model.challenge.WeeklyChallengeProofCreateRequest
import com.makeus.mody.core.network.model.record.ImageCropRegionDto
import com.makeus.mody.core.network.model.unwrapResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버 응답을 그대로 노출한다. 실패는 예외로, 빈 응답은 빈 목록으로 화면까지 전달한다 —
 * "데이터가 없는 것"과 "부르는 데 실패한 것"을 화면이 구분할 수 있어야 한다.
 */
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
                nudgeStatus = NudgeButtonStatus.from(
                    raw = it.buttonStatus,
                    recordedToday = it.recordedToday,
                    nudgedToday = it.nudgedToday,
                ),
            )
        }

    override suspend fun nudge(groupId: Long, memberId: Long): NudgeButtonStatus {
        val r = challengeApi.nudge(groupId, memberId).unwrapResult()
        return NudgeButtonStatus.from(
            raw = r.buttonStatus,
            recordedToday = false,
            // 서버가 값을 줬으면 그대로 믿는다. 아예 안 준 경우(구버전)에만 성공한 POST 라는
            // 사실로 true 를 채운다 — 명시적 false 까지 덮어쓰면 서버 판단을 뒤집는다.
            nudgedToday = r.nudgedToday ?: true,
        )
    }

    override suspend fun getStepChallenge(groupId: Long): StepChallengeStatus {
        val s = challengeApi.getStepChallenge(groupId).unwrapResult()
        return StepChallengeStatus(
            groupChallengeId = s.groupChallengeId,
            title = s.title,
            targetStepCount = s.targetStepCount,
            currentStepCount = s.currentStepCount,
            fetchFromAt = s.fetchFromAt.toServerInstantOrNull(),
        )
    }

    override suspend fun getStepChallengeOptions(groupId: Long): List<StepChallengeOption> =
        challengeApi.getStepChallengeOptions(groupId).unwrapResult().options.map {
            StepChallengeOption(
                challengeId = it.challengeId,
                title = it.title,
                departure = it.departure,
                destination = it.destination,
                distanceKm = it.distanceKm,
                targetStepCount = it.targetStepCount,
                selected = it.selected,
            )
        }

    override suspend fun changeStepChallenge(
        groupId: Long,
        challengeId: Long,
    ): StepChallengeStatus {
        val r = challengeApi.changeStepChallenge(
            groupId = groupId,
            request = StepChallengeChangeRequest(challengeId = challengeId),
        ).unwrapResult()
        return StepChallengeStatus(
            groupChallengeId = r.groupChallengeId,
            title = r.title,
            targetStepCount = r.targetStepCount,
            currentStepCount = r.currentStepCount,
            fetchFromAt = r.fetchFromAt.toServerInstantOrNull(),
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
                participants = it.participants.map { p ->
                    WeeklyChallengeParticipant(
                        memberId = p.memberId,
                        nickname = p.nickname,
                        profileImageUrl = p.profileImageUrl,
                    )
                },
            )
        }

    override suspend fun getWeeklyChallengeProofs(
        groupId: Long,
        groupChallengeId: Long,
    ): List<WeeklyChallengeProof> =
        challengeApi.getWeeklyChallengeProofs(groupId, groupChallengeId)
            .unwrapResult().proofs.map {
                WeeklyChallengeProof(
                    proofId = it.proofId,
                    imageUrl = it.imageUrl,
                    cropRegion = it.imageCropRegion?.toCropRegion(),
                    memberId = it.memberId,
                    nickname = it.nickname,
                    profileImageUrl = it.profileImageUrl,
                )
            }

    override suspend fun createWeeklyChallengeProof(
        groupId: Long,
        groupChallengeId: Long,
        imageKey: String,
        cropRegion: CropRegion?,
    ): WeeklyChallengeProof {
        val r = challengeApi.createWeeklyChallengeProof(
            groupId = groupId,
            groupChallengeId = groupChallengeId,
            request = WeeklyChallengeProofCreateRequest(
                imageKey = imageKey,
                imageCropRegion = cropRegion?.let {
                    ImageCropRegionDto(x = it.x, y = it.y, width = it.width, height = it.height)
                },
            ),
        ).unwrapResult()
        // 등록 응답엔 작성자 정보가 없다. 화면은 등록 후 목록을 다시 받아 채운다.
        return WeeklyChallengeProof(
            proofId = r.proofId,
            imageUrl = r.imageUrl,
            cropRegion = r.imageCropRegion?.toCropRegion(),
            memberId = 0,
            nickname = "",
            profileImageUrl = null,
        )
    }

    override suspend fun shareWeeklyChallenge(
        groupId: Long,
        groupChallengeId: Long,
    ): WeeklyChallengeShare {
        val r = challengeApi.shareWeeklyChallenge(groupId, groupChallengeId).unwrapResult()
        return WeeklyChallengeShare(
            imageUrl = r.imageUrl,
            cropRegion = r.imageCropRegion?.toCropRegion(),
            rows = r.rows,
            columns = r.columns,
        )
    }

    private fun ImageCropRegionDto.toCropRegion(): CropRegion =
        CropRegion(x = x, y = y, width = width, height = height)

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
