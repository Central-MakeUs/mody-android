package com.makeus.mody.feature.group.share

import android.content.Context
import android.content.Intent
import com.makeus.mody.feature.group.BuildConfig
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient

/**
 * 그룹 초대 코드를 카카오톡으로 공유.
 * 카카오톡 설치 시 톡 공유, 미설치 시 웹 공유(브라우저)로 폴백.
 *
 * 카드 구성(이미지·문구·버튼·실행 링크)은 카카오 개발자 콘솔 메시지 템플릿에서 관리한다.
 * buildType 별 템플릿: debug=모디 DEV(135810), release=모디 PROD(135811) — BuildConfig 주입.
 * 콘솔 템플릿이 참조하는 사용자 인자: ${code}(초대 코드), ${groupName}(그룹명).
 * 콘솔에서 인자명이 바뀌면 여기 templateArgs 키도 함께 맞춰야 한다.
 */
object KakaoInviteSharer {

    private val TEMPLATE_ID = BuildConfig.KAKAO_SHARE_TEMPLATE_ID

    fun share(context: Context, code: String, groupName: String, onError: (Throwable) -> Unit) {
        // 그룹명 미확보 시(공유 진입 경로에 따라 빈 값일 수 있음) 자연스러운 문구로 폴백.
        val templateArgs = mapOf(
            "code" to code,
            "groupName" to groupName.ifBlank { "모디" },
        )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            ShareClient.instance.shareCustom(context, TEMPLATE_ID, templateArgs) { result, error ->
                when {
                    error != null -> onError(error)
                    result != null -> context.startActivity(result.intent)
                    else -> onError(IllegalStateException("카카오 공유 결과 없음"))
                }
            }
        } else {
            // 카카오톡 미설치 → 웹 공유(브라우저)
            runCatching {
                val url = WebSharerClient.instance.makeCustomUrl(TEMPLATE_ID, templateArgs)
                context.startActivity(Intent(Intent.ACTION_VIEW, url))
            }.onFailure(onError)
        }
    }
}
