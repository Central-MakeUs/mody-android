package com.makeus.mody.core.domain.usecase

import com.makeus.mody.core.domain.model.StartDestination
import com.makeus.mody.core.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * 앱 시작 시 진입 목적지를 결정한다. (우선순위 순)
 *
 * 1. 미인증 → AUTH (로그인)
 * 2. 개인정보(온보딩) 미완료 → ONBOARDING
 * 3. mainAccessible == true → MAIN (속한 그룹 있고 회원가입 완료)
 * 4. 그 외 → GROUP (회원가입 완료: 그룹 참여/생성)
 *    - groupOnboardingCompleted true 이지만 그룹을 다 삭제한 예외 케이스 포함
 *      (mainAccessible=false 면 무조건 GROUP → groupOnboardingCompleted 는 분기에 미사용)
 */
class ResolveStartDestinationUseCase @Inject constructor(
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): StartDestination {
        if (!session.isLoggedIn()) return StartDestination.AUTH
        val status = session.getStatus()
        return when {
            !status.personalInfoCompleted -> StartDestination.ONBOARDING
            status.mainAccessible -> StartDestination.MAIN
            else -> StartDestination.GROUP
        }
    }
}
