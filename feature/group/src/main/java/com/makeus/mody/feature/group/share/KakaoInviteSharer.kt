package com.makeus.mody.feature.group.share

import android.content.Context
import android.content.Intent
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate

/**
 * 그룹 초대 코드를 카카오톡으로 공유.
 * 카카오톡 설치 시 톡 공유, 미설치 시 웹 공유(브라우저)로 폴백.
 *
 * 주의: link 의 URL 도메인은 카카오 개발자 콘솔 > 앱 > 플랫폼 > Web 사이트 도메인에
 * 등록돼 있어야 공유가 동작한다(미등록 시 도메인 오류).
 */
object KakaoInviteSharer {

    // TODO(group): 초대 랜딩/딥링크 URL 확정 시 교체. 도메인은 카카오 콘솔에 등록 필요.
    private const val INVITE_URL = "https://dev-mody.store"

    fun share(context: Context, code: String, onError: (Throwable) -> Unit) {
        val template = TextTemplate(
            text = "모디에 초대되었어요!\n초대 코드: $code\n함께 건강한 다이어트 습관을 만들어요.",
            link = Link(mobileWebUrl = INVITE_URL, webUrl = INVITE_URL),
        )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            ShareClient.instance.shareDefault(context, template) { result, error ->
                when {
                    error != null -> onError(error)
                    result != null -> context.startActivity(result.intent)
                    else -> onError(IllegalStateException("카카오 공유 결과 없음"))
                }
            }
        } else {
            // 카카오톡 미설치 → 웹 공유(브라우저)
            runCatching {
                val url = WebSharerClient.instance.makeDefaultUrl(template)
                context.startActivity(Intent(Intent.ACTION_VIEW, url))
            }.onFailure(onError)
        }
    }
}
