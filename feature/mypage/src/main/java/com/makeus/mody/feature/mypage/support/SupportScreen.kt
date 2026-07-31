package com.makeus.mody.feature.mypage.support

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
import com.makeus.mody.feature.mypage.support.contract.SupportIntent

@Composable
fun SupportScreen(viewModel: SupportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 하드웨어/제스처 뒤로가기도 상단바 뒤로와 동일하게 화면 종료.
    BackHandler { viewModel.onIntent(SupportIntent.BackClicked) }

    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "문의 및 약관확인",
                onBackClick = { viewModel.onIntent(SupportIntent.BackClicked) },
            )
        },
    ) {
        SupportWebView(
            url = state.url,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SupportWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // 하드코딩된 자체 지원 페이지(변조 위험 없음) → 내부 약관/개인정보/문의 링크 이동 허용.
                webViewClient = WebViewClient()
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
