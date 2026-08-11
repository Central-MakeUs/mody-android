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
 *    - 그룹을 전부 나가 mainAccessible=false 가 된 경우도 여기로 온다.
 *
 * 이 규칙은 여기 한 곳에만 둔다. 로그인 직후 진입도 같은 UseCase 를 태워서
 * "로그인 직후"와 "앱 재시작"이 서로 다른 화면으로 갈라지지 않게 한다.
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
