package com.makeus.mody.feature.group.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link

/**
 * 그룹 초대 코드를 카카오톡으로 공유.
 * 카카오톡 설치 시 톡 공유, 미설치 시 웹 공유(브라우저)로 폴백.
 *
 * 주의: link 의 URL 도메인은 카카오 개발자 콘솔 > 앱 > 플랫폼 > Web 사이트 도메인에
 * 등록돼 있어야 공유가 동작한다(미등록 시 도메인 오류).
 */
object KakaoInviteSharer {

    // 초대 App Link. iOS(Universal Link)와 동일 포맷: /invite?code=XXX.
    // 도메인은 카카오 콘솔 > 앱 > 플랫폼 > Web 사이트 도메인에 등록 필요.
    private const val INVITE_BASE_URL = "https://dev-mody.store/invite"

    // TODO(group): 초대 카드 대표 이미지(2:1). 현재는 카카오 공식 샘플로 테스트.
    private const val SHARE_IMAGE_URL =
        "https://mud-kage.kakao.com/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png"

    fun share(context: Context, code: String, onError: (Throwable) -> Unit) {
        val inviteUrl = "$INVITE_BASE_URL?code=${Uri.encode(code)}"
        // executionParams: 수신자 카카오톡이 설치 여부를 판단해
        // 설치 → kakao{키}://kakaolink?code=XXX 로 앱 즉시 실행 / 미설치 → 마켓 이동.
        // web/mobileWebUrl 은 PC 카카오톡·웹 공유 폴백용으로 유지.
        val executionParams = mapOf("code" to code)
        val link = Link(
            mobileWebUrl = inviteUrl,
            webUrl = inviteUrl,
            androidExecutionParams = executionParams,
            iosExecutionParams = executionParams,
        )
        val template = FeedTemplate(
            content = Content(
                title = "모디에 초대되었어요!",
                description = "초대 코드: $code\n함께 건강한 다이어트 습관을 만들어요.",
                imageUrl = SHARE_IMAGE_URL,
                link = link,
            ),
            buttons = listOf(
                Button(title = "앱에서 열기", link = link),
            ),
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
