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
 *
 * ## 콘솔에 있어야 하는 설정 (코드로는 확인 불가)
 *
 * **수신자 분기(설치=앱 실행 / 미설치=마켓)는 콘솔 템플릿 버튼의 앱 실행 파라미터가 한다.**
 * 버튼에 `code=${code}` 를 android/iOS 양쪽에 넣고, 앱 설정 > 플랫폼 > Android 에
 * 패키지명과 **마켓 URL** 이 등록돼 있어야 한다. 마켓 URL 이 비면 미설치자가 갈 데가 없어
 * 웹 링크로 폴백하고, 그 랜딩 페이지는 안드로이드를 분기하지 않아 App Store 로 튕긴다.
 *
 * 설치된 기기는 `kakao{네이티브키}://kakaolink?code=XXX` 로 앱이 열린다 — 수신부는
 * presentation manifest 의 intent-filter 와 MainActivity.handleInviteDeepLink 에 있다.
 *
 * 이 설정은 원래 코드에 있었고(6405f01, `Link.androidExecutionParams`), shareCustom 전환
 * (27b8954)에서 콘솔로 넘어갔다. 그때 콘솔에 옮겨 넣지 않아 전환 이전 동작(웹 URL 로만
 * 이동)으로 되돌아갔다. **템플릿을 새로 만들거나 갈아탈 때 이 항목을 함께 옮겨라.**
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
