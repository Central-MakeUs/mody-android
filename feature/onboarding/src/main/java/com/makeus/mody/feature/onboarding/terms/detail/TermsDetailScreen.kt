package com.makeus.mody.feature.onboarding.terms.detail

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.feature.onboarding.terms.detail.contract.TermsDetailIntent

@Composable
fun TermsDetailScreen(viewModel: TermsDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 하드웨어/제스처 뒤로가기도 상단바 뒤로와 동일하게 화면 종료.
    BackHandler { viewModel.onIntent(TermsDetailIntent.BackClicked) }

    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = state.title,
                onBackClick = { viewModel.onIntent(TermsDetailIntent.BackClicked) },
            )
        },
    ) {
        TermsWebView(
            url = state.url,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TermsWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    // 최초 로드 URL 의 호스트만 허용 — 변조/리다이렉트로 약관 외 페이지가 뜨는 것을 차단.
    val allowedHost = remember(url) { Uri.parse(url).host }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val target = request.url
                        val allowed = target.scheme.equals("https", ignoreCase = true) &&
                            target.host == allowedHost
                        // true = 앱이 가로채 로드 취소(허용 호스트 밖 이동 차단).
                        return !allowed
                    }
                }
                // 자체 호스팅(GitHub Pages) 정적 페이지. SPA 호환 위해 JS/DOM 스토리지 허용.
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView ->
            if (url.isNotBlank() && webView.url != url) {
                webView.loadUrl(url)
            }
        },
    )
}
