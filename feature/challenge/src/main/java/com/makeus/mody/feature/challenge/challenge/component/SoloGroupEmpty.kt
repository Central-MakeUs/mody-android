package com.makeus.mody.feature.challenge.challenge.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.challenge.challenge.contract.ChallengeSubTab

/** 시안의 일러스트 프레임 높이. 두 탭이 같은 값이라 텍스트 위치도 같아진다. */
private val IllustrationBoxHeight = 60.dp

/** 프레임 대비 그림 크기·위치. 에셋이 그림 크기로 잘려 나와 프레임 안 여백은 여기서 준다. */
private val StreakIllustrationSize = DpSize(66.dp, 43.dp)
private val StreakIllustrationTop = 7.dp
private val ChallengeIllustrationSize = DpSize(68.dp, 60.dp)

/**
 * 나 혼자인 그룹 — 연속 기록도 챌린지도 성립하지 않아 탭 내용을 통째로 대신한다.
 *
 * 시안: 연속 기록 `251:3370`, 챌린지 `251:3201` (모디 MODY export용, uQWUtLv8xzOFNrthwozXs9).
 * 두 탭이 일러스트와 두 번째 줄만 다르고 나머지는 같아 한 컴포저블로 묶었다.
 */
@Composable
fun SoloGroupEmpty(
    tab: ChallengeSubTab,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 높이를 프레임에 고정해 두 탭의 텍스트 위치를 같게 맞춘다. 연속 기록 일러스트는
        // 프레임보다 작아(시안에서 위쪽 8~49 에만 그려짐) 안에서 위로 붙인다.
        Box(
            modifier = Modifier.height(IllustrationBoxHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            Image(
                painter = painterResource(
                    when (tab) {
                        ChallengeSubTab.STREAK -> R.drawable.img_streak_empty
                        ChallengeSubTab.CHALLENGE -> R.drawable.img_challenge_empty
                    },
                ),
                contentDescription = null,
                modifier = when (tab) {
                    ChallengeSubTab.STREAK -> Modifier
                        .padding(top = StreakIllustrationTop)
                        .size(StreakIllustrationSize)
                    ChallengeSubTab.CHALLENGE -> Modifier.size(ChallengeIllustrationSize)
                },
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "아직 함께하는 멤버가 없어요",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (tab) {
                ChallengeSubTab.STREAK -> "멤버를 초대하면 연속 기록을 시작할 수 있어요!"
                ChallengeSubTab.CHALLENGE -> "멤버를 초대하면 챌린지를 시작할 수 있어요!"
            },
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray06,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, name = "혼자인 그룹 - 연속 기록")
@Composable
private fun SoloGroupEmptyStreakPreview() {
    ModyTheme { SoloGroupEmpty(tab = ChallengeSubTab.STREAK) }
}

@Preview(showBackground = true, name = "혼자인 그룹 - 챌린지")
@Composable
private fun SoloGroupEmptyChallengePreview() {
    ModyTheme { SoloGroupEmpty(tab = ChallengeSubTab.CHALLENGE) }
}
