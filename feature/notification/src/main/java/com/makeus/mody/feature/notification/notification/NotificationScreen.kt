package com.makeus.mody.feature.notification.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.notification.R
import com.makeus.mody.feature.notification.notification.contract.NotificationIntent
import com.makeus.mody.feature.notification.notification.contract.NotificationState
import com.makeus.mody.feature.notification.notification.contract.NotificationUiModel

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationScreen(
        state = state,
        onBackClick = { viewModel.onIntent(NotificationIntent.BackClicked) },
        onLoadMore = { viewModel.onIntent(NotificationIntent.LoadMore) },
    )
}

@Composable
private fun NotificationScreen(
    state: NotificationState,
    onBackClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ModyBackTopBar(title = "알림", onBackClick = onBackClick)

        when {
            state.isEmpty -> NotificationEmptyContent(modifier = Modifier.fillMaxSize())
            else -> NotificationList(
                notifications = state.notifications,
                onLoadMore = onLoadMore,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun NotificationList(
    notifications: List<NotificationUiModel>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // 마지막에서 3칸 이내로 스크롤되면 다음 페이지 요청. 페이지 종료/중복은 ViewModel 이 가드한다.
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(state = listState, modifier = modifier) {
        items(notifications, key = { it.id }) { item ->
            NotificationRow(item)
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.isRead) ModyTheme.colors.white else ModyTheme.colors.primary400)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
            )
            Text(
                text = item.description,
                style = ModyTheme.typography.c1,
                color = ModyTheme.colors.gray05,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.timeText,
            style = ModyTheme.typography.c3,
            color = ModyTheme.colors.gray04,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun NotificationEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_notification_empty),
            contentDescription = null,
            modifier = Modifier.size(width = 48.dp, height = 56.dp),
        )
        Spacer(Modifier.size(24.dp))
        Text(
            text = "새로운 알림이 없어요.",
            style = ModyTheme.typography.b3,
            color = ModyTheme.colors.gray10,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "새로운 소식이 생기면 여기에서 알려드릴게요.",
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray06,
        )
    }
}

@Preview
@Composable
private fun NotificationScreenPreview() {
    ModyTheme {
        NotificationScreen(
            state = NotificationState(
                isInitialLoaded = true,
                notifications = listOf(
                    NotificationUiModel(
                        id = 1,
                        iconRes = com.makeus.mody.core.designsystem.R.drawable.ic_party,
                        title = "아자아자 그룹에 새 버디가 참여했어요!",
                        description = "키드님을 환영해주세요!",
                        timeText = "1일 전",
                        isRead = false,
                    ),
                    NotificationUiModel(
                        id = 2,
                        iconRes = com.makeus.mody.core.designsystem.R.drawable.ic_comment,
                        title = "예은님이 댓글을 남겼어요.",
                        description = "어떤 이야기를 남겼는지 확인하러 가요!",
                        timeText = "1일 전",
                        isRead = true,
                    ),
                ),
            ),
            onBackClick = {},
            onLoadMore = {},
        )
    }
}
