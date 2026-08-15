package com.makeus.mody.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepChallengeStatusTest {

    private fun status(
        current: Int,
        target: Int,
        progress: StepChallengeProgress = StepChallengeProgress.IN_PROGRESS,
    ) = StepChallengeStatus(
        groupChallengeId = 1,
        title = "수원까지 걸어가기",
        targetStepCount = target,
        currentStepCount = current,
        progress = progress,
    )

    // --- 서버 enum 매핑 ---

    @Test
    fun `서버가 준 상태를 그대로 매핑한다`() {
        assertEquals(StepChallengeProgress.IN_PROGRESS, StepChallengeProgress.from("IN_PROGRESS"))
        assertEquals(StepChallengeProgress.COMPLETED, StepChallengeProgress.from("COMPLETED"))
        assertEquals(StepChallengeProgress.RESET, StepChallengeProgress.from("RESET"))
        assertEquals(StepChallengeProgress.CANCELED, StepChallengeProgress.from("CANCELED"))
    }

    /**
     * 서버가 상태를 추가해도 앱이 죽으면 안 된다. 모르는 값은 UNKNOWN 으로 받고
     * 화면은 진행 중과 같이 다뤄 최소한 걸음 수는 보이게 한다.
     */
    @Test
    fun `모르는 값과 없는 값은 UNKNOWN 이다`() {
        assertEquals(StepChallengeProgress.UNKNOWN, StepChallengeProgress.from(null))
        assertEquals(StepChallengeProgress.UNKNOWN, StepChallengeProgress.from(""))
        assertEquals(StepChallengeProgress.UNKNOWN, StepChallengeProgress.from("PAUSED"))
        // 대소문자가 다르면 다른 값이다 — 서버 enum 을 그대로 비교한다.
        assertEquals(StepChallengeProgress.UNKNOWN, StepChallengeProgress.from("completed"))
    }

    @Test
    fun `COMPLETED 일 때만 완료로 본다`() {
        assertTrue(status(150_000, 150_000, StepChallengeProgress.COMPLETED).isCompleted)
        for (other in StepChallengeProgress.entries - StepChallengeProgress.COMPLETED) {
            assertFalse("$other 는 완료가 아니다", status(150_000, 150_000, other).isCompleted)
        }
    }

    // --- 달성률 ---

    /** 완료 후에도 걸음 수는 계속 쌓인다. 안 막으면 게이지가 한 바퀴를 넘어간다. */
    @Test
    fun `목표를 넘겨도 100 에서 멈춘다`() {
        assertEquals(100, status(150_000, 150_000).progressPercent)
        assertEquals(100, status(320_000, 150_000).progressPercent)
    }

    @Test
    fun `달성률은 정수 내림이다`() {
        assertEquals(0, status(0, 150_000).progressPercent)
        assertEquals(50, status(75_000, 150_000).progressPercent)
        // 99.99% 를 100 으로 올리면 목표 전인데 다 찬 것처럼 보인다.
        assertEquals(99, status(149_999, 150_000).progressPercent)
    }

    /** 목표가 0 이면 나눌 수 없다. 예외 대신 0 을 준다. */
    @Test
    fun `목표가 0 이면 0 이다`() {
        assertEquals(0, status(10_000, 0).progressPercent)
        assertEquals(0, status(10_000, -1).progressPercent)
    }
}
