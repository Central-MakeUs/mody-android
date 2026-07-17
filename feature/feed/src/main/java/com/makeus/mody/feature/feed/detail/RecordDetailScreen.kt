package com.makeus.mody.feature.feed.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.R
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
    val author = state.records.getOrNull(state.currentIndex)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModyTheme.colors.white)
            .statusBarsPadding(),
    ) {
        ModyBackTopBar(
            title = author?.authorName.orEmpty(),
            onBackClick = { onIntent(RecordDetailIntent.BackClicked) },
            showAvatar = true,
            avatarUrl = author?.avatarUrl,
        )

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
    // 페이지 정착 시 currentIndex 갱신 (탑바 작성자/페이지 점). 댓글은 통일이라 재조회 안 함.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { onIntent(RecordDetailIntent.PageChanged(it)) }
    }

    // 피드 헤더 높이(px). 댓글 목록을 이 높이만큼 밀어, 스크롤 시 댓글이 헤더 뒤로 숨게 함.
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(LocalDensity.current) { headerHeightPx.toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        // 댓글: 헤더 뒤로 스크롤되어 숨음. 헤더 높이만큼 top content padding.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeightDp, bottom = 12.dp),
        ) {
            if (state.comments.isEmpty() && !state.isCommentsLoading) {
                item { EmptyComments() }
            } else {
                items(state.comments, key = { it.id }) { comment ->
                    CommentBubble(comment)
                }
            }
        }

        // 피드 헤더: 상단 고정 + 불투명 흰 배경 → 댓글이 이 뒤로 숨는다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ModyTheme.colors.white)
                .onGloballyPositioned { headerHeightPx = it.size.height },
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                // contentPadding 0 → 이웃 카드 미리보기(peek) 제거. 좌우 여백은 페이지 내부에서.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) { page ->
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    FeedCard(card = state.records[page], onClick = {}, showHeader = false)
                }
            }
            if (state.records.size > 1) {
                PageDots(
                    current = state.currentIndex,
                    total = state.records.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/** 페이지 점 인디케이터. 현재 = primary100 알약, 나머지 = gray02 점. */
@Composable
private fun PageDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (active) 16.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (active) ModyTheme.colors.primary100 else ModyTheme.colors.gray02),
            )
        }
    }
}

/** 응원 댓글 없음 상태. */
@Composable
private fun EmptyComments() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.img_comment_empty),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(width = 45.dp, height = 57.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 응원 댓글이 없어요",
            style = ModyTheme.typography.b4,
            color = ModyTheme.colors.gray10,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "첫 응원 한마디를 남겨보세요!",
            style = ModyTheme.typography.b5,
            color = ModyTheme.colors.gray06,
        )
    }
}

/** 응원 댓글 버블. 내 댓글 = 우측 primary300, 남의 댓글 = 좌측 gray01. */
@Composable
private fun CommentBubble(comment: CommentUi) {
    val bubbleShape = RoundedCornerShape(16.dp)
    if (comment.isMine) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = comment.authorName,
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray06,
                )
                ModyAvatar(imageUrl = comment.avatarUrl, size = 28.dp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = ModyTheme.typography.b7,
                color = ModyTheme.colors.gray10,
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(ModyTheme.colors.primary300)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ModyAvatar(imageUrl = comment.avatarUrl, size = 28.dp)
            Column {
                Text(
                    text = comment.authorName,
                    style = ModyTheme.typography.c1,
                    color = ModyTheme.colors.gray06,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = ModyTheme.typography.b7,
                    color = ModyTheme.colors.gray10,
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(ModyTheme.colors.gray01)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
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
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .border(1.dp, ModyTheme.colors.gray02, CircleShape),
            placeholder = {
                Text(
                    text = "버디에게 응원 한마디를 남겨보세요!",
                    style = ModyTheme.typography.b7,
                    color = ModyTheme.colors.gray04,
                )
            },
            textStyle = ModyTheme.typography.b5,
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
        if (canSend) {
            IconButton(onClick = onSend) {
                Icon(
                    painter = painterResource(ModyIcons.SendFilled),
                    contentDescription = "전송",
                    tint = Color.Unspecified,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun RecordDetailContentPreview() {
    val records = listOf(
        FeedCardUi(
            id = 1, authorName = "난화영이다", dayCount = 3,
            primaryLabel = "식사 시간", primaryValue = "13:00",
            secondaryLabel = "메뉴", secondaryValue = "계란 3알, 사과 2조각",
        ),
    )
    val comments = listOf(
        CommentUi(1, "예은", null, "계란 3알로 오늘 어떻게 버텨요?? ㅠㅠㅠ", false),
        CommentUi(2, "동준", null, "파이팅 합시다!!!!", false),
        CommentUi(3, "난화영이다", null, "주스는 제로 칼로리라서..", true),
    )
    ModyTheme {
        RecordDetailContent(
            state = RecordDetailState(isLoading = false, records = records, comments = comments),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun RecordDetailEmptyPreview() {
    val records = listOf(
        FeedCardUi(
            id = 1, authorName = "난화영이다", dayCount = 5,
            primaryLabel = "운동 시간", primaryValue = "68분",
            secondaryLabel = "운동종류", secondaryValue = "필라테스",
        ),
    )
    ModyTheme {
        RecordDetailContent(
            state = RecordDetailState(isLoading = false, records = records, comments = emptyList()),
            onIntent = {},
        )
    }
}
