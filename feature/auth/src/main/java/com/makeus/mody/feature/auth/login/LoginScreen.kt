package com.makeus.mody.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "MODY",
            modifier = Modifier.size(96.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.login_title_line1),
            style = ModyTheme.typography.h1,
            color = ModyTheme.colors.gray10,
        )

        // 2번째 줄 글자 바로 밑에 노란 하이라이트(밑줄) 깔기
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.ic_text_highlight),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.login_title_line2),
                style = ModyTheme.typography.h1,
                color = ModyTheme.colors.gray10,
            )
        }

        Spacer(modifier = Modifier.weight(1.4f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
            .padding(bottom = 86.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModyButton(
                text = stringResource(R.string.login_kakao),
                onClick = onKakaoLoginClick,
                variant = ModyButtonVariant.Kakao,
                leadingIcon = R.drawable.ic_kakao,
            )
            ModyButton(
                text = stringResource(R.string.login_google),
                onClick = onGoogleLoginClick,
                variant = ModyButtonVariant.Google,
                leadingIcon = R.drawable.ic_google,
            )
        }
    }
}
