package com.makeus.mody.presentation.notification

import com.makeus.mody.core.navigation.RecordGraph
import com.makeus.mody.core.navigation.Route

/**
 * 알림 딥링크 목적지. 서버가 보내는 link(URL path)를 파싱한 결과.
 *  - [Screen]: 그대로 push 할 라우트(운동/식사 기록 입력).
 *  - [GroupHome]: 별도 라우트가 없어 Feed 탭 + 그룹 전환으로 처리(groupId 만 넘김).
 */
sealed interface NotificationDestination {
    data class Screen(val route: Route) : NotificationDestination
    data class GroupHome(val groupId: Long) : NotificationDestination
}

/**
 * 서버 알림 link(URL path) → 앱 목적지 매핑.
 * 지원 경로(이번 스코프 4종):
 *  - /records/exercise, /records/exercise/new → 운동 기록 입력
 *  - /records/meal, /records/meal/new         → 식사 기록 입력
 *  - /groups/{groupId}/home          → 해당 그룹 홈(버디 참여/어디가셨나요)
 * 미지원/파싱 실패는 null → 호출부에서 무시(엉뚱한 화면 이동 방지).
 */
object NotificationLinkParser {
    fun parse(link: String): NotificationDestination? {
        // 쿼리스트링 제거 후 경로 세그먼트만. 앞뒤 슬래시 무시.
        val segments = link.substringBefore('?').trim('/').split('/').filter { it.isNotBlank() }
        // "/", "///" 등은 blank 가 아니어도 세그먼트가 비어 segments[0] 접근 시 크래시 → 선제 차단.
        if (segments.isEmpty()) return null
        return when {
            // 서버 실제 발송값은 "/records/meal" 형태(/new 없음) — 둘 다 허용.
            segments[0] == "records" && (segments.size == 2 || (segments.size == 3 && segments[2] == "new")) ->
                when (segments[1]) {
                    "exercise" -> NotificationDestination.Screen(RecordGraph.HealthRoute)
                    "meal" -> NotificationDestination.Screen(RecordGraph.FoodRoute)
                    else -> null
                }

            segments.size == 3 && segments[0] == "groups" && segments[2] == "home" ->
                segments[1].toLongOrNull()?.let { NotificationDestination.GroupHome(it) }

            else -> null
        }
    }
}
