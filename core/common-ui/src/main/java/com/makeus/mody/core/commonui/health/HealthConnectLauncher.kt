package com.makeus.mody.core.commonui.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient
import com.makeus.mody.core.domain.model.HealthAvailability

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

/**
 * 건강 데이터 연동 설정 진입.
 * - 사용 가능: 우리 앱의 Health Connect 권한 화면 → (액션 미지원 기기면) Health Connect 홈.
 * - 미설치/업데이트 필요: 플레이스토어의 Health Connect 페이지 → (스토어 앱 없으면) 웹.
 *
 * 앞의 것부터 시도하고 열리는 첫 인텐트를 쓴다. 전부 실패하는 기기가 있어 토스트로 마무리.
 *
 * 마이페이지(연동 설정)와 챌린지 탭(권한 거부 안내)이 같이 쓴다 — 기기별 폴백 순서가 까다로워
 * 화면마다 따로 두면 한쪽만 고쳐지는 일이 생긴다.
 */
fun openHealthConnect(context: Context, availability: HealthAvailability) {
    val candidates = when (availability) {
        HealthAvailability.AVAILABLE -> listOf(
            // Android 14+ 는 플랫폼이, 그 이하는 Health Connect 앱이 처리하는 별개 액션.
            Intent(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    "android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"
                } else {
                    "androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS"
                },
            ).putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName),
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS),
        )

        HealthAvailability.UPDATE_REQUIRED, HealthAvailability.UNAVAILABLE -> listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"),
            ),
        )
    }
    val launched = candidates.any { intent ->
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .isSuccess
    }
    if (!launched) {
        Toast.makeText(context, "건강 데이터 설정을 열 수 없어요.", Toast.LENGTH_SHORT).show()
    }
}
