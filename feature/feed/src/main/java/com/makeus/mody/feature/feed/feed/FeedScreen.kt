package com.makeus.mody.feature.feed.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.R
import com.makeus.mody.feature.feed.feed.component.FeedCalendarSheet
import com.makeus.mody.feature.feed.feed.component.FeedCard
import com.makeus.mody.feature.feed.feed.component.FeedWriteFab
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState

@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedTopBar(
                onMembersClick = { viewModel.onIntent(FeedIntent.MembersClicked) },
                onAlarmClick = { viewModel.onIntent(FeedIntent.AlarmClicked) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            DateSelector(
                label = state.dateLabel,
                onClick = { viewModel.onIntent(FeedIntent.DateClicked) },
            )

            if (state.isEmpty) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    FeedEmptyContent(
                        onPokeClick = { viewModel.onIntent(FeedIntent.PokeClicked) },
                    )
                }
            } else {
                FeedList(
                    state = state,
                    onCardClick = { id -> viewModel.onIntent(FeedIntent.FeedCardClicked(id)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        FeedWriteFab(
            expanded = state.isFabExpanded,
            onFabClick = { viewModel.onIntent(FeedIntent.FabClicked) },
            onDismiss = { viewModel.onIntent(FeedIntent.FabDismissed) },
            onWriteExercise = { viewModel.onIntent(FeedIntent.WriteExerciseClicked) },
            onWriteMeal = { viewModel.onIntent(FeedIntent.WriteMealClicked) },
        )
    }

    if (state.isCalendarVisible) {
        FeedCalendarSheet(
            title = state.calendarTitle,
            days = state.calendarDays,
            onPrevMonth = { viewModel.onIntent(FeedIntent.CalendarPrevMonth) },
            onNextMonth = { viewModel.onIntent(FeedIntent.CalendarNextMonth) },
            onDaySelected = { day -> viewModel.onIntent(FeedIntent.CalendarDaySelected(day)) },
            onConfirm = { viewModel.onIntent(FeedIntent.CalendarConfirmClicked) },
            onDismiss = { viewModel.onIntent(FeedIntent.CalendarDismissed) },
        )
    }
}

/** 피드 카드 목록 (Feed2 시안). */
@Composable
private fun FeedList(
    state: FeedState,
    onCardClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(state.feeds, key = { it.id }) { card ->
            FeedCard(
                card = card,
                onClick = { onCardClick(card.id) },
            )
        }
    }
}

/** 상단 바: MODY 로고 + 그룹 멤버/알림 아이콘. */
@Composable
private fun FeedTopBar(
    onMembersClick: () -> Unit,
    onAlarmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(ModyIcons.LogoWordmark),
            contentDescription = "MODY",
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMembersClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(ModyIcons.User),
                    contentDescription = "그룹 멤버",
                    tint = ModyTheme.colors.gray10,
                )
            }
            IconButton(onClick = onAlarmClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(ModyIcons.Alarm),
                    contentDescription = "알림",
                    tint = ModyTheme.colors.gray10,
                )
            }
        }
    }
}

/** 날짜 셀렉터: "7월 18일" + 아래 화살표. 탭 → 캘린더(Feed3). */
@Composable
private fun DateSelector(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )
        Icon(
            painter = painterResource(ModyIcons.Down),
            contentDescription = "날짜 선택",
            tint = ModyTheme.colors.gray10,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** 오늘 올라온 피드가 없을 때 (Feed1 시안). */
@Composable
private fun FeedEmptyContent(
    onPokeClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_feed_empty),
            contentDescription = null,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "오늘 피드를 올린 분이 없어요",
                style = ModyTheme.typography.b3,
                color = ModyTheme.colors.gray10,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "콕찌르기를 통해 알려주세요!",
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray06,
            )
        }
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ModyTheme.colors.primary100)
                .clickable(onClick = onPokeClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "콕 찌르기 하러 가기",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
        }
    }
}
