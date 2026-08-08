package com.makeus.mody.feature.feed.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.component.CroppedAsyncImage
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi

// TODO(designsystem): 불꽃 주황 토큰 확정 시 교체
private val FireOrange = Color(0xFFFF5C00)

/**
 * 피드 카드: 작성자 헤더 + 기록 이미지 카드 (Feed2 시안).
 * showHeader=false 면 헤더(아바타·이름·N일차) 생략 — 상세 화면처럼 탑바가 이미 작성자 정보를 보일 때.
 * showArrow=false 면 우상단 화살표(→상세 이동) 생략 — 상세 화면처럼 더 들어갈 곳이 없을 때.
 *
 * 미트볼(⋯) 메뉴는 [onReportClick]/[onDeleteClick] 중 지정된 것만 항목으로 낸다.
 * 남의 글이면 신고, 내 글이면 삭제 — 둘 다 null 이면 메뉴 자체를 안 그린다.
 */
@Composable
fun FeedCard(
    card: FeedCardUi,
    onClick: () -> Unit,
    showHeader: Boolean = true,
    showArrow: Boolean = true,
    onReportClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showHeader) {
            FeedCardHeader(
                authorName = card.authorName,
                avatarUrl = card.avatarUrl,
                dayCount = card.dayCount,
                onReportClick = onReportClick,
                onDeleteClick = onDeleteClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        FeedCardImage(card = card, onClick = onClick, showArrow = showArrow)
    }
}

@Composable
private fun FeedCardHeader(
    authorName: String,
    avatarUrl: String?,
    dayCount: Int,
    onReportClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyAvatar(
            imageUrl = avatarUrl,
            contentDescription = "프로필",
            size = 32.dp,
        )
        Text(
            text = authorName,
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray09,
        )
        DayCountChip(dayCount = dayCount)
        if (onReportClick != null || onDeleteClick != null) {
            Spacer(modifier = Modifier.weight(1f))
            FeedCardMenu(onReportClick = onReportClick, onDeleteClick = onDeleteClick)
        }
    }
}

/** 미트볼(⋯) 버튼 + 드롭다운. 넘어온 동작만 항목으로 낸다. */
@Composable
private fun FeedCardMenu(
    onReportClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Icon(
            painter = painterResource(ModyIcons.Meatball),
            contentDescription = "더보기",
            tint = ModyTheme.colors.gray05,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(10.dp),
            containerColor = ModyTheme.colors.white,
        ) {
            if (onReportClick != null) {
                FeedCardMenuItem(
                    text = "신고",
                    color = ModyTheme.colors.gray09,
                    onClick = {
                        expanded = false
                        onReportClick()
                    },
                )
            }
            if (onDeleteClick != null) {
                // 되돌릴 수 없는 동작이라 색으로도 구분한다(확인 다이얼로그는 화면이 띄운다).
                FeedCardMenuItem(
                    text = "삭제",
                    color = ModyTheme.colors.error,
                    onClick = {
                        expanded = false
                        onDeleteClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun FeedCardMenuItem(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = ModyTheme.typography.c1,
        color = color,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

/** "N일차 🔥" 칩. */
@Composable
private fun DayCountChip(dayCount: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(ModyTheme.colors.gray01)
            .border(
                width = 0.4.dp,
                color = ModyTheme.colors.gray03,
                shape = RoundedCornerShape(100.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${dayCount}일차",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray10,
        )
        Icon(
            painter = painterResource(ModyIcons.FireFill),
            contentDescription = null,
            tint = FireOrange,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun FeedCardImage(
    card: FeedCardUi,
    onClick: () -> Unit,
    showArrow: Boolean = true,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ModyTheme.colors.gray04) // 로딩 전/실패 시 플레이스홀더
            .clickable(onClick = onClick),
    ) {
        // 기록 사진(원본 + 크롭 영역). url null/로딩 실패 시 gray04 배경만 보임.
        CroppedAsyncImage(
            model = card.imageUrl,
            contentDescription = null,
            cropX = card.cropRegion?.x,
            cropY = card.cropRegion?.y,
            cropWidth = card.cropRegion?.width,
            cropHeight = card.cropRegion?.height,
            modifier = Modifier.fillMaxSize(),
        )
        // 하단 그라데이션 (텍스트 가독성)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        startY = 200f * 0.3f * 3, // 대략 30% 지점부터 (px 근사)
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column {
                Text(
                    text = card.primaryLabel,
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray01,
                )
                Text(
                    text = card.primaryValue,
                    style = ModyTheme.typography.b1,
                    color = ModyTheme.colors.white,
                )
            }
            Column {
                Text(
                    text = card.secondaryLabel,
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray01,
                )
                Text(
                    text = card.secondaryValue,
                    style = ModyTheme.typography.b2,
                    color = ModyTheme.colors.white,
                )
            }
        }
        if (showArrow) {
            Icon(
                painter = painterResource(ModyIcons.Right),
                contentDescription = "댓글 보기",
                tint = ModyTheme.colors.white,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(24.dp),
            )
        }
    }
}
