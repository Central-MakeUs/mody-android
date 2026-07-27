package com.makeus.mody.feature.onboarding.terms.detail

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
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
