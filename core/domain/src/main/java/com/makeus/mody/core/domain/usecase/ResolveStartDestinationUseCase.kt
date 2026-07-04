package com.makeus.mody.core.domain.usecase

import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * 앱 시작(콜드 스타트) 시 진입 목적지를 결정한다.
 *
 * 규칙:
 * - 미인증 → AUTH
 * - 인증됐지만 프로필(온보딩) 미완료 → AUTH (온보딩 하다 만 경우 무조건 로그인부터)
 * - 그룹 온보딩 미완료 → GROUP
 * - 전부 완료 → MAIN
 *
 * 참고: "로그인 직후" 라우팅은 LoginViewModel 이 따로 처리(온보딩 미완이면 바로 온보딩 진행).
 * 여기는 재접속 흐름이라 온보딩 미완 = 로그인부터.
 */
class ResolveStartDestinationUseCase @Inject constructor(
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): StartDestination {
        if (!session.isLoggedIn()) return StartDestination.AUTH
        val status = session.getStatus()
        return when {
            !status.personalInfoCompleted -> StartDestination.AUTH
            !status.groupOnboardingCompleted -> StartDestination.GROUP
            else -> StartDestination.MAIN
        }
    }
}
