package com.makeus.mody.feature.challenge.weeklydetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.camera.ModyCameraOverlay
import com.makeus.mody.core.camera.SQUARE_FRAME_RATIO
import com.makeus.mody.core.designsystem.component.CroppedAsyncImage
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyChip
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyLoadingScreen
import com.makeus.mody.core.designsystem.component.ModyScreenScaffold
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.core.domain.model.WeeklyChallengeProof
import com.makeus.mody.feature.challenge.util.dDayLabel
import com.makeus.mody.feature.challenge.weeklydetail.contract.WeeklyChallengeDetailIntent
import com.makeus.mody.feature.challenge.weeklydetail.contract.WeeklyChallengeDetailState

/** 인증 사진 그리드 열 수. 시안 2열(354 = 172 * 2 + 10). */
private const val ProofColumns = 2

/** 시안 실측 값. */
private val ScreenPadding = 24.dp
private val ProofGridGap = 10.dp
private val ProofCornerRadius = 12.dp

/** 닉네임을 읽히게 하는 상단 그라데이션 높이(셀 172 중 90). */
private val ProofScrimHeight = 90.dp
private val ProofAvatarSize = 30.dp

/** 주간 챌린지 상세 — 인증 사진 그리드 + SNS 공유. */
@Composable
fun WeeklyChallengeDetailScreen(
    viewModel: WeeklyChallengeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.onIntent(WeeklyChallengeDetailIntent.ScreenEntered) }

    WeeklyChallengeDetailContent(state = state, onIntent = viewModel::onIntent)

    // "인증하기" 는 바로 촬영으로 간다 — 인증 사진이라 그 자리에서 찍는 게 기본 흐름.
    // 갤러리는 막는다(onPickGallery = null): 아무 사진이나 고르면 인증이 성립하지 않는다.
    if (state.isCameraVisible) {
        ModyCameraOverlay(
            frameRatio = SQUARE_FRAME_RATIO,
            onConfirm = { uri, region ->
                viewModel.onIntent(WeeklyChallengeDetailIntent.PhotoCaptured(uri, region))
            },
            onPickGallery = null,
            onDismiss = { viewModel.onIntent(WeeklyChallengeDetailIntent.CameraDismissed) },
        )
    }

    // 콜라주가 준비되면 공유 시트로. URI 는 한 번만 쓰고 비운다.
    state.shareImageUri?.let { uri ->
        LaunchedEffect(uri) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "인증 사진 공유하기"))
            viewModel.onIntent(WeeklyChallengeDetailIntent.ShareLaunched)
        }
    }

    ModyErrorDialog(
        message = state.error,
        onDismiss = { viewModel.onIntent(WeeklyChallengeDetailIntent.ErrorShown) },
    )
}

@Composable
private fun WeeklyChallengeDetailContent(
    state: WeeklyChallengeDetailState,
    onIntent: (WeeklyChallengeDetailIntent) -> Unit,
) {
    ModyScreenScaffold(
        topBar = {
            ModyBackTopBar(
                title = "주간 챌린지",
                onBackClick = { onIntent(WeeklyChallengeDetailIntent.BackClicked) },
            )
        },
    ) {
        if (state.isLoading) {
            ModyLoadingScreen()
            return@ModyScreenScaffold
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ChallengeHeader(
                    title = state.title,
                    description = state.description,
                    deadlineDayOfWeek = state.deadlineDayOfWeek,
                )
                // 시안 실측 13 (카드 끝 207 → 그리드 220).
                Spacer(modifier = Modifier.height(13.dp))
                ProofGrid(
                    proofs = state.proofs,
                    isUploading = state.isUploading,
                    onAddClick = { onIntent(WeeklyChallengeDetailIntent.AddProofClicked) },
                )
                // 시안 실측 30 (그리드 끝 756 → 버튼 786). 버튼이 스크롤 밖에 고정이라
                // 이 값은 끝까지 내렸을 때의 최소 간격이 된다.
                Spacer(modifier = Modifier.height(30.dp))
            }
            ModyButton(
                text = if (state.isPreparingShare) "공유 이미지 만드는 중…" else "SNS에 공유하기",
                onClick = { onIntent(WeeklyChallengeDetailIntent.ShareClicked) },
                variant = ModyButtonVariant.Dark,
                // 인증 사진이 없으면 서버가 만들 게 없다.
                enabled = state.proofs.isNotEmpty() && !state.isPreparingShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

/**
 * 제목 + 설명 + D-N 칩.
 *
 * 칩은 제목과 같은 줄 오른쪽 끝(시안 top 16 고정)이라 Row 로 묶지 않고 Box 상단에 붙인다.
 * 제목이 길어 줄바꿈돼도 칩 위치가 안 흔들린다.
 */
@Composable
private fun ChallengeHeader(title: String, description: String, deadlineDayOfWeek: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ProofCornerRadius))
            .background(ModyTheme.colors.primary400)
            .padding(16.dp),
    ) {
        Column(
            // 칩과 겹치지 않도록 텍스트 폭을 칩 자리만큼 줄인다.
            modifier = Modifier.padding(end = 52.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray08,
                )
            }
        }
        ModyChip(
            text = dDayLabel(deadlineDayOfWeek),
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

/**
 * 첫 칸이 "+ 인증하기", 나머지가 인증 사진인 2열 격자.
 *
 * LazyVerticalGrid 를 안 쓴다 — 이 화면은 바깥이 이미 세로 스크롤이라 중첩되면 높이가 무한이 된다.
 * 인증 사진은 그룹 인원 수만큼이라 전부 그려도 부담이 없다.
 */
@Composable
private fun ProofGrid(
    proofs: List<WeeklyChallengeProof>,
    isUploading: Boolean,
    onAddClick: () -> Unit,
) {
    // 첫 칸(추가 버튼) + 사진들을 한 줄에 두 칸씩.
    val cells: List<WeeklyChallengeProof?> = listOf(null) + proofs
    Column(verticalArrangement = Arrangement.spacedBy(ProofGridGap)) {
        cells.chunked(ProofColumns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ProofGridGap)) {
                row.forEach { proof ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (proof == null) {
                            AddProofCell(isUploading = isUploading, onClick = onAddClick)
                        } else {
                            ProofCell(proof = proof)
                        }
                    }
                }
                // 마지막 줄이 한 칸이면 남는 자리를 채워 셀 폭을 유지한다.
                repeat(ProofColumns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AddProofCell(isUploading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ProofCornerRadius))
            .background(ModyTheme.colors.gray01)
            .clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isUploading) {
            Text(
                text = "올리는 중…",
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray09,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(ModyIcons.Plus),
                    contentDescription = null,
                    tint = ModyTheme.colors.gray09,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "인증하기",
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray09,
                )
            }
        }
    }
}

@Composable
private fun ProofCell(proof: WeeklyChallengeProof) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ProofCornerRadius))
            .background(ModyTheme.colors.gray02),
    ) {
        CroppedAsyncImage(
            model = proof.imageUrl,
            contentDescription = "${proof.nickname}님의 인증 사진",
            cropX = proof.cropRegion?.x,
            cropY = proof.cropRegion?.y,
            cropWidth = proof.cropRegion?.width,
            cropHeight = proof.cropRegion?.height,
            modifier = Modifier.fillMaxSize(),
        )
        // 밝은 사진 위에서도 닉네임이 읽히도록 상단만 어둡게.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProofScrimHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                    ),
                ),
        )
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModyAvatar(imageUrl = proof.profileImageUrl, size = ProofAvatarSize)
            Text(
                text = proof.nickname,
                style = ModyTheme.typography.c2,
                color = ModyTheme.colors.white,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun WeeklyChallengeDetailContentPreview() {
    ModyTheme {
        WeeklyChallengeDetailContent(
            state = WeeklyChallengeDetailState(
                title = "엘레베이터 안 타고 집가기",
                description = "집까지 계단으로 이동한 사진을 인증해주세요!",
                deadlineDayOfWeek = "SUNDAY",
                isLoading = false,
                proofs = listOf(
                    WeeklyChallengeProof(1, "", null, 1, "모나", null),
                    WeeklyChallengeProof(2, "", null, 2, "키드", null),
                    WeeklyChallengeProof(3, "", null, 3, "도모", null),
                ),
            ),
            onIntent = {},
        )
    }
}
