package com.makeus.mody.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyAvatarSkeleton
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyChip
import com.makeus.mody.core.designsystem.component.ModyChipStyle
import com.makeus.mody.core.designsystem.component.ModyLoadingIndicator
import com.makeus.mody.core.designsystem.component.ModySwitch
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.component.ModyTextSkeleton
import com.makeus.mody.core.designsystem.theme.ModyTheme

/**
 * 디자인 시스템 컴포넌트 카탈로그.
 *
 * 왜 필요한가 — 공용 컴포넌트가 있는지 몰라서 화면마다 따로 그리는 일이 반복됐다.
 * 시안의 `chip` 하나가 코드에서 두 규격으로 갈라졌던 게 그 결과다. 어떤 컴포넌트가
 * 어떤 variant 로 있는지 한 화면에서 볼 수 있으면 "없는 줄 알고 새로 그리는" 경우가 준다.
 *
 * Android Studio 의 Preview 로 본다. 앱에 넣지 않는 이유는 릴리스 빌드에 카탈로그가
 * 딸려 들어가지 않게 하기 위해서다 — Preview 는 debug 도구 체인에서만 렌더된다.
 *
 * 컴포넌트를 추가하면 여기도 추가한다. 여기 없으면 없는 것과 같다.
 */
@Composable
private fun ModyCatalog() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModyTheme.colors.white)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        CatalogSection("Chip") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModyChip(text = "완료")
                ModyChip(text = "D-4", style = ModyChipStyle.Dark)
            }
        }

        CatalogSection("Button") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (variant in ModyButtonVariant.entries) {
                    ModyButton(
                        text = variant.name,
                        onClick = {},
                        variant = variant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                ModyButton(
                    text = "Disabled",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        CatalogSection("TextField") {
            var value by remember { mutableStateOf("") }
            ModyTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "입력해주세요",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        CatalogSection("Switch") {
            var checked by remember { mutableStateOf(true) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModySwitch(checked = checked, onCheckedChange = { checked = it })
                ModySwitch(checked = false, onCheckedChange = {}, enabled = false)
            }
        }

        CatalogSection("Avatar") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModyAvatar(imageUrl = null, size = 24.dp)
                ModyAvatar(imageUrl = null, size = 32.dp)
                ModyAvatar(imageUrl = null, size = 48.dp)
            }
        }

        CatalogSection("Skeleton") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModyTextSkeleton(width = 160.dp, lineHeight = ModyTheme.typography.b5.lineHeight)
                ModyTextSkeleton(width = 96.dp, lineHeight = ModyTheme.typography.c2.lineHeight)
                ModyAvatarSkeleton(size = 32.dp)
            }
        }

        CatalogSection("Loading") {
            ModyLoadingIndicator()
        }
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
        )
        content()
    }
}

@Preview(name = "Component Catalog", showBackground = true, heightDp = 1400)
@Composable
private fun ModyCatalogPreview() {
    ModyTheme { ModyCatalog() }
}
