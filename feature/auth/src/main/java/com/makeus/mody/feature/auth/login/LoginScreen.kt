package com.makeus.mody.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.auth.R
import com.makeus.mody.feature.auth.login.contract.LoginIntent

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    LoginContent(
        onKakaoLoginClick = { viewModel.onIntent(LoginIntent.KakaoLoginClicked) },
        onGoogleLoginClick = { viewModel.onIntent(LoginIntent.GoogleLoginClicked) },
    )
}

@Composable
private fun LoginContent(
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "친구와 함께하는\n다이어트 습관",
            style = ModyTheme.typography.h1,
            color = ModyTheme.colors.gray10,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
            .padding(bottom = 86.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModyButton(
                text = "카카오로 시작하기",
                onClick = onKakaoLoginClick,
                variant = ModyButtonVariant.Kakao,
                leadingIcon = R.drawable.ic_kakao,
            )
            ModyButton(
                text = "Google로 시작하기",
                onClick = onGoogleLoginClick,
                variant = ModyButtonVariant.Google,
                leadingIcon = R.drawable.ic_google,
            )
        }
    }
}
