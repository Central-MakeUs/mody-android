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

    // 초대 카드 대표 이미지(2:1). GCS 공개 객체 — 카카오가 스크랩하려면 공개 읽기여야 함.
    // (버킷: mody-images. storage.cloud.google.com 이 아닌 공개 직링크 storage.googleapis.com 사용)
    private const val SHARE_IMAGE_URL =
        "https://storage.googleapis.com/mody-images/profiles/" +
            "%E1%84%86%E1%85%A9%E1%84%83%E1%85%B5%20%E1%84%8F%E1%85%A1%E1%84%90%E1%85%A9%E1%86%A8%20" +
            "%E1%84%80%E1%85%A9%E1%86%BC%E1%84%8B%E1%85%B2%20%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.png"

    fun share(context: Context, code: String, groupName: String, onError: (Throwable) -> Unit) {
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
        // 그룹명 미확보 시(공유 진입 경로에 따라 빈 값일 수 있음) 자연스러운 문구로 폴백.
        val inviteLine = if (groupName.isNotBlank()) {
            "$groupName 그룹에서 함께하고 싶어요!"
        } else {
            "그룹에서 함께하고 싶어요!"
        }
        val template = FeedTemplate(
            content = Content(
                title = "그룹 코드 : $code",
                description = "$inviteLine\n그룹 참여하기에서 코드를 입력해보세요.",
                imageUrl = SHARE_IMAGE_URL,
                link = link,
            ),
            buttons = listOf(
                Button(title = "모디로 이동하기", link = link),
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
