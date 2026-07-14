package com.makeus.mody.feature.feed.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.makeus.mody.core.designsystem.component.ModyBackButton
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.detail.contract.CommentUi
import com.makeus.mody.feature.feed.detail.contract.RecordDetailIntent
import com.makeus.mody.feature.feed.detail.contract.RecordDetailState
import com.makeus.mody.feature.feed.feed.component.FeedCard
import com.makeus.mody.feature.feed.feed.contract.FeedCardUi

@Composable
fun RecordDetailScreen(viewModel: RecordDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecordDetailContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
private fun RecordDetailContent(
    state: RecordDetailState,
    onIntent: (RecordDetailIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding(),
    ) {
        DetailTopBar(onBack = { onIntent(RecordDetailIntent.BackClicked) })

        when {
            state.isLoading -> LoadingBox()
            state.records.isEmpty() -> Box(modifier = Modifier.fillMaxSize())
            else -> DetailBody(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
        }

        CommentInputBar(
            value = state.commentInput,
            canSend = state.canSend,
            onChange = { onIntent(RecordDetailIntent.CommentInputChanged(it)) },
            onSend = { onIntent(RecordDetailIntent.SendCommentClicked) },
        )
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModyBackButton(onClick = onBack)
        Text(
            text = "기록",
            style = ModyTheme.typography.b6,
            color = ModyTheme.colors.gray10,
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ModyTheme.colors.primary100)
    }
}

@Composable
private fun DetailBody(
    state: RecordDetailState,
    onIntent: (RecordDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { state.records.size })

    // 상세 진입 위치로 이동 (레코드 로드 후 1회).
    LaunchedEffect(state.records.size) {
        if (state.records.isNotEmpty()) pagerState.scrollToPage(state.currentIndex)
    }
    // 페이지 정착 시 해당 기록 댓글 로드.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { onIntent(RecordDetailIntent.PageChanged(it)) }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) { page ->
                FeedCard(card = state.records[page], onClick = {})
            }
            if (state.records.size > 1) {
                PageIndicator(
                    current = state.currentIndex,
                    total = state.records.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                )
            }
        }

        item {
            Text(
                text = "댓글 ${state.comments.size}",
                style = ModyTheme.typography.b6,
                color = ModyTheme.colors.gray10,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        items(state.comments, key = { it.id }) { comment ->
            CommentRow(comment)
        }
    }
}

@Composable
private fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "${current + 1} / $total",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.gray06,
        )
    }
}

@Composable
private fun CommentRow(comment: CommentUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(
            model = comment.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ModyTheme.colors.gray03),
        )
        Column {
            Text(
                text = comment.authorName,
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray09,
            )
            Text(
                text = comment.content,
                style = ModyTheme.typography.b5,
                color = ModyTheme.colors.gray10,
            )
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    canSend: Boolean,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape),
            placeholder = { Text("댓글을 입력하세요", style = ModyTheme.typography.b5) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ModyTheme.colors.gray01,
                unfocusedContainerColor = ModyTheme.colors.gray01,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        IconButton(onClick = onSend, enabled = canSend) {
            Icon(
                painter = painterResource(ModyIcons.Right),
                contentDescription = "전송",
                tint = if (canSend) ModyTheme.colors.primary100 else ModyTheme.colors.gray04,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordDetailContentPreview() {
    val records = listOf(
        FeedCardUi(
            id = 1, authorName = "도모", dayCount = 3,
            primaryLabel = "식사 시간", primaryValue = "13:00",
            secondaryLabel = "메뉴", secondaryValue = "계란 3알, 사과 2조각",
        ),
    )
    val comments = listOf(
        CommentUi(1, "지훈", null, "오 맛있겠다", false),
        CommentUi(2, "도모", null, "ㅎㅎ 고마워", true),
    )
    ModyTheme {
        RecordDetailContent(
            state = RecordDetailState(
                isLoading = false,
                records = records,
                comments = comments,
            ),
            onIntent = {},
        )
    }
}
