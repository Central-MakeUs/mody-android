package com.makeus.mody.feature.feed.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.R
import com.makeus.mody.feature.feed.feed.component.FeedCard
import com.makeus.mody.feature.feed.feed.component.FeedWeekSection
import com.makeus.mody.feature.feed.feed.component.FeedWriteFab
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi
import com.makeus.mody.feature.feed.feed.contract.FeedIntent
import com.makeus.mody.feature.feed.feed.contract.FeedState
import com.makeus.mody.feature.feed.feed.contract.GroupUi
import com.makeus.mody.feature.feed.feed.contract.WeekDayUi
import java.time.LocalDate

/** 실제 진입점: ViewModel 상태를 stateless [FeedContent] 로 흘려보낸다. */
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeedContent(state = state, onIntent = viewModel::onIntent)
}

/** 상태를 받아 렌더만 하는 stateless 화면. 프리뷰/테스트는 여기에 직접 상태를 넣는다. */
@Composable
private fun FeedContent(
    state: FeedState,
    onIntent: (FeedIntent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedTopBar(
                onAlarmClick = { onIntent(FeedIntent.AlarmClicked) },
            )
            GroupSelector(
                groupName = state.groupName,
                onClick = { onIntent(FeedIntent.GroupSelectorClicked) },
            )
            FeedWeekSection(
                weekLabel = state.weekLabel,
                weekDays = state.weekDays,
                onPrevWeek = { onIntent(FeedIntent.PrevWeekClicked) },
                onNextWeek = { onIntent(FeedIntent.NextWeekClicked) },
                onDaySelected = { date -> onIntent(FeedIntent.DaySelected(date)) },
            )

            if (state.isEmpty) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    FeedEmptyContent(
                        onPokeClick = { onIntent(FeedIntent.PokeClicked) },
                    )
                }
            } else {
                FeedList(
                    state = state,
                    onCardClick = { id -> onIntent(FeedIntent.FeedCardClicked(id)) },
                    onLoadMore = { onIntent(FeedIntent.LoadMoreFeeds) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        FeedWriteFab(
            expanded = state.isFabExpanded,
            onFabClick = { onIntent(FeedIntent.FabClicked) },
            onDismiss = { onIntent(FeedIntent.FabDismissed) },
            onWriteExercise = { onIntent(FeedIntent.WriteExerciseClicked) },
            onWriteMeal = { onIntent(FeedIntent.WriteMealClicked) },
        )
    }

    if (state.isGroupSheetVisible) {
        GroupSelectSheet(
            groups = state.groups,
            onSelect = { id -> onIntent(FeedIntent.GroupSelected(id)) },
            onAddGroup = { onIntent(FeedIntent.AddGroupClicked) },
            onDismiss = { onIntent(FeedIntent.GroupSheetDismissed) },
        )
    }
}

/** 피드 카드 목록 (Feed_기본 시안). 끝에 다다르면 다음 페이지 로드. */
@Composable
private fun FeedList(
    state: FeedState,
    onCardClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // 마지막에서 3칸 이내로 스크롤되면 다음 페이지 요청.
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            state.hasMoreFeeds && !state.isLoadingMore && last >= state.feeds.size - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
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
        if (state.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = ModyTheme.colors.primary100,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/** 상단 바: MODY 로고 + 알림 아이콘. */
@Composable
private fun FeedTopBar(
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
        IconButton(onClick = onAlarmClick, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(ModyIcons.Alarm),
                contentDescription = "알림",
                tint = ModyTheme.colors.gray10,
            )
        }
    }
}

/** 그룹명 셀렉터: "아자아자" + 화살표. 탭 → 그룹 선택 시트. */
@Composable
private fun GroupSelector(
    groupName: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = groupName,
                style = ModyTheme.typography.b2,
                color = ModyTheme.colors.gray10,
            )
            Icon(
                painter = painterResource(ModyIcons.Up),
                contentDescription = "그룹 선택",
                tint = ModyTheme.colors.gray10,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** 그룹 선택 바텀시트 (Feed_그룹 선택 시안). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSelectSheet(
    groups: List<GroupUi>,
    onSelect: (Long) -> Unit,
    onAddGroup: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ModyTheme.colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groups.forEach { group ->
                GroupSelectRow(group = group, onClick = { onSelect(group.id) })
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ModyTheme.colors.gray01)
                    .clickable(onClick = onAddGroup),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "그룹 추가하기 +",
                    style = ModyTheme.typography.b5,
                    color = ModyTheme.colors.gray06,
                )
            }
        }
    }
}

@Composable
private fun GroupSelectRow(group: GroupUi, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val base = Modifier
        .fillMaxWidth()
        .clip(shape)
        .then(
            if (group.isCurrent) {
                Modifier
                    .background(ModyTheme.colors.primary0)
                    .border(1.dp, ModyTheme.colors.secondary100, shape)
            } else {
                Modifier.background(ModyTheme.colors.gray01)
            },
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 14.dp)
    Row(
        modifier = base,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = group.name,
                    style = ModyTheme.typography.b3,
                    color = ModyTheme.colors.gray10,
                )
                Text(
                    text = " 그룹",
                    style = ModyTheme.typography.b6,
                    color = ModyTheme.colors.gray06,
                )
            }
            Text(
                text = "그룹코드 ${group.code}",
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray06,
            )
        }
        if (group.isCurrent) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ModyTheme.colors.secondary100)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "현재 보는 중",
                    style = ModyTheme.typography.b7,
                    color = ModyTheme.colors.gray10,
                )
            }
        }
    }
}

/** 선택한 날짜에 올라온 피드가 없을 때 (Feed_존재 안 함 시안). */
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

// ---------------------------------------------------------------------------
// 프리뷰: 더미 상태는 여기에만 존재 → 앱 런타임 경로에는 더미 없음.
// Android Studio Split/Design 탭에서 확인. 기기 실행 불필요.
// ---------------------------------------------------------------------------

private fun previewWeekDays(selected: LocalDate): List<WeekDayUi> {
    val sunday = LocalDate.of(2026, 7, 12)
    val labels = listOf("일", "월", "화", "수", "목", "금", "토")
    return (0..6).map { i ->
        val date = sunday.plusDays(i.toLong())
        WeekDayUi(
            date = date,
            weekdayLabel = labels[i],
            isSelected = date == selected,
            hasFeed = i % 2 == 1, // 격일로 점 표시
        )
    }
}

private val PREVIEW_FEEDS = listOf(
    FeedCardUi(
        id = 1,
        authorName = "난화영이다",
        dayCount = 2,
        primaryLabel = "식사 시간",
        primaryValue = "13:00",
        secondaryLabel = "메뉴",
        secondaryValue = "계란 3알, 사과 2조각",
    ),
    FeedCardUi(
        id = 2,
        authorName = "모나",
        dayCount = 5,
        primaryLabel = "운동 시간",
        primaryValue = "45분",
        secondaryLabel = "운동종류",
        secondaryValue = "런닝",
    ),
    FeedCardUi(
        id = 3,
        authorName = "난화영이다",
        dayCount = 2,
        primaryLabel = "운동 시간",
        primaryValue = "68분",
        secondaryLabel = "운동종류",
        secondaryValue = "필라테스",
    ),
)

private val PREVIEW_BASE_STATE = FeedState(
    groupName = "아자아자",
    weekLabel = "7월 2주차",
    weekDays = previewWeekDays(LocalDate.of(2026, 7, 16)),
)

/** 피드 목록 있는 상태. */
@Preview(name = "Feed - 목록", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun FeedContentListPreview() {
    ModyTheme {
        FeedContent(
            state = PREVIEW_BASE_STATE.copy(feeds = PREVIEW_FEEDS),
            onIntent = {},
        )
    }
}

/** 빈 상태. */
@Preview(name = "Feed - 빈 상태", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun FeedContentEmptyPreview() {
    ModyTheme {
        FeedContent(
            state = PREVIEW_BASE_STATE.copy(feeds = emptyList()),
            onIntent = {},
        )
    }
}

/** FAB 확장 상태. */
@Preview(name = "Feed - FAB 확장", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun FeedContentFabExpandedPreview() {
    ModyTheme {
        FeedContent(
            state = PREVIEW_BASE_STATE.copy(feeds = PREVIEW_FEEDS, isFabExpanded = true),
            onIntent = {},
        )
    }
}
