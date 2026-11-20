package com.makeus.mody.feature.feed.detail

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.makeus.mody.feature.feed.detail.contract.CommentSegment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.InlineTextContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeus.mody.core.designsystem.component.ModyAvatar
import com.makeus.mody.core.designsystem.component.ModyBackTopBar
import com.makeus.mody.core.designsystem.component.ModyDialog
import com.makeus.mody.core.designsystem.component.ModyErrorDialog
import com.makeus.mody.core.designsystem.component.ModyLoadingOverlay
import com.makeus.mody.core.designsystem.component.ModyLoadingScreen
import com.makeus.mody.core.designsystem.component.ModyPageIndicator
import com.makeus.mody.core.designsystem.component.ModyPopupMenu
import com.makeus.mody.core.designsystem.component.ModyPopupMenuItem
import com.makeus.mody.core.designsystem.icon.ModyIcons
import com.makeus.mody.core.designsystem.theme.ModyTheme
import com.makeus.mody.feature.feed.R
import com.makeus.mody.feature.feed.detail.contract.CommentEmoji
import com.makeus.mody.feature.feed.detail.contract.CommentUi
import com.makeus.mody.feature.feed.detail.contract.RecordDetailIntent
import com.makeus.mody.feature.feed.detail.contract.RecordDetailState
import com.makeus.mody.feature.feed.feed.component.FeedCard
import com.makeus.mody.feature.feed.feed.component.FeedCardMenu
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
    val author = state.currentRecord
    val isMine = state.isCurrentMine
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
            titleTrailing = if (isMine == true) {
                { MeBadge() }
            } else {
                null
            },
            // 남의 글이면 신고, 내 글이면 삭제. 판별 전(null)엔 메뉴를 숨긴다(피드 카드와 같은 정책).
            trailingContent = if (isMine == null) {
                null
            } else {
                {
                    FeedCardMenu(
                        onReportClick = { onIntent(RecordDetailIntent.ReportClicked) }.takeIf { !isMine },
                        onDeleteClick = { onIntent(RecordDetailIntent.DeleteClicked) }.takeIf { isMine },
                    )
                }
            },
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

        // 응원 댓글 입력바: 기능이 열렸을 때만 노출(닫히면 전송도 차단).
        if (state.commentEnabled) {
            EmojiBar(
                // 전송 중에도 누를 수 있다 — 미리 붙고 순서대로 보내진다(도배 제한은 ViewModel 이 한다).
                enabled = state.records.isNotEmpty(),
                onClick = { onIntent(RecordDetailIntent.EmojiClicked(it)) },
            )
            CommentInputBar(
                value = state.commentInput,
                canSend = state.canSend,
                onChange = { onIntent(RecordDetailIntent.CommentInputChanged(it)) },
                onSend = { onIntent(RecordDetailIntent.SendCommentClicked) },
            )
        }
    }

    if (state.isReporting || state.isDeleting || state.isDeletingComment) ModyLoadingOverlay()

    // 이모지 도배 제한 안내. 화면을 막을 일이 아니라 다이얼로그 대신 토스트.
    val context = LocalContext.current
    LaunchedEffect(state.emojiLimitNotice) {
        val notice = state.emojiLimitNotice ?: return@LaunchedEffect
        Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
        onIntent(RecordDetailIntent.EmojiLimitNoticeShown)
    }
    RecordActionDialogs(state = state, onIntent = onIntent)
}

/** 내 글 표시 "나" 배지(시안 9:3784). primary300 원 24dp. */
@Composable
private fun MeBadge() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(ModyTheme.colors.primary300),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "나", style = ModyTheme.typography.c2, color = ModyTheme.colors.gray07)
    }
}

/** 신고·삭제 확인/완료/실패 다이얼로그. 문구는 피드 카드 메뉴와 같다. */
@Composable
private fun RecordActionDialogs(
    state: RecordDetailState,
    onIntent: (RecordDetailIntent) -> Unit,
) {
    if (state.reportTargetRecordId != null) {
        ModyDialog(
            title = "게시물을 신고하시겠어요?",
            message = "신고한 게시물은 검토 후 삭제 처리할 예정입니다.",
            confirmText = "신고",
            dismissText = "취소",
            confirmEnabled = !state.isReporting,
            onConfirm = { onIntent(RecordDetailIntent.ReportConfirmed) },
            onDismissRequest = { onIntent(RecordDetailIntent.ReportDialogDismissed) },
        )
    }

    if (state.showReportComplete) {
        ModyDialog(
            title = "신고가 완료되었어요",
            message = "검토 및 처리는 최대 7일까지 걸릴 수 있어요.",
            confirmText = "확인",
            onConfirm = { onIntent(RecordDetailIntent.ReportCompleteConfirmed) },
            onDismissRequest = { onIntent(RecordDetailIntent.ReportCompleteConfirmed) },
        )
    }

    if (state.deleteTargetRecordId != null) {
        ModyDialog(
            title = "게시물을 삭제하시겠어요?",
            message = "한번 삭제한 게시물은 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            confirmEnabled = !state.isDeleting,
            onConfirm = { onIntent(RecordDetailIntent.DeleteConfirmed) },
            onDismissRequest = { onIntent(RecordDetailIntent.DeleteDialogDismissed) },
        )
    }

    ModyErrorDialog(
        message = state.reportError,
        onDismiss = { onIntent(RecordDetailIntent.ReportErrorShown) },
    )

    ModyErrorDialog(
        message = state.deleteError,
        onDismiss = { onIntent(RecordDetailIntent.DeleteErrorShown) },
    )

    ModyErrorDialog(
        message = state.sendError,
        onDismiss = { onIntent(RecordDetailIntent.SendErrorShown) },
    )

    if (state.commentDeleteTargetId != null) {
        ModyDialog(
            title = "댓글을 삭제하시겠어요?",
            message = "한번 삭제한 댓글은 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            confirmEnabled = !state.isDeletingComment,
            onConfirm = { onIntent(RecordDetailIntent.CommentDeleteConfirmed) },
            onDismissRequest = { onIntent(RecordDetailIntent.CommentDeleteDialogDismissed) },
        )
    }

    ModyErrorDialog(
        message = state.commentDeleteError,
        onDismiss = { onIntent(RecordDetailIntent.CommentDeleteErrorShown) },
    )
}

@Composable
private fun LoadingBox() {
    ModyLoadingScreen()
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
        // 채팅처럼 최신 댓글이 맨 아래. reverseLayout 이라 0번(=최신)이 아래에 붙고, 키보드가 올라와
        // 목록이 줄어도 아래(최신)가 유지된다. 빈 상태 문구는 위에서부터 보여야 해 그때만 정방향.
        val comments = state.visibleComments
        val hasComments = comments.isNotEmpty()
        val listState = rememberLazyListState()
        val newestId = comments.lastOrNull()?.id
        // 기록을 넘기거나(댓글이 바뀜) 내가 보내 새 댓글이 붙으면 최신으로 내린다.
        LaunchedEffect(state.currentRecord?.id, newestId) {
            if (hasComments) listState.scrollToItem(0)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = hasComments,
            contentPadding = PaddingValues(top = headerHeightDp, bottom = 12.dp),
        ) {
            // 응원 댓글: 기능이 열렸을 때만 노출(빈 상태 문구 포함).
            if (state.commentEnabled) {
                if (!hasComments && !state.isCommentsLoading) {
                    item { EmptyComments() }
                } else {
                    items(comments.asReversed(), key = { it.id }) { comment ->
                        CommentBubble(
                            comment = comment,
                            onDeleteClick = { onIntent(RecordDetailIntent.CommentDeleteClicked(comment.id)) },
                        )
                    }
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
                    FeedCard(card = state.records[page], onClick = null, showHeader = false)
                }
            }
            if (state.records.size > 1) {
                ModyPageIndicator(
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

/**
 * 응원 댓글 한 줄(시안 9:3643).
 * - 남의 댓글: 아바타·이름 줄 아래에 말풍선(gray01, 왼쪽 위만 각짐) + 오른쪽에 시각.
 * - 내 댓글: 아바타·이름 없이 오른쪽 정렬, 왼쪽에 시각 + 말풍선(primary300, 오른쪽 위만 각짐).
 *   꾹 누르면 말풍선 아래에 "삭제"가 뜬다.
 * - 이모지 댓글은 말풍선 없이 이모지만(44dp).
 */
@Composable
private fun CommentBubble(comment: CommentUi, onDeleteClick: () -> Unit) {
    if (comment.isMine) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                // 보내는 중인 미리보기는 반투명.
                .alpha(if (comment.isPending) PENDING_ALPHA else 1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
            verticalAlignment = Alignment.Bottom,
        ) {
            CommentTime(comment.timeLabel)
            // 시각 자리를 남기고 말풍선이 줄바꿈되도록 weight(fill=false).
            Box(modifier = Modifier.weight(1f, fill = false)) {
                DeletableCommentBody(comment = comment, onDeleteClick = onDeleteClick)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModyAvatar(imageUrl = comment.avatarUrl, size = 32.dp)
                Text(
                    text = comment.authorName,
                    style = ModyTheme.typography.c2,
                    color = ModyTheme.colors.gray07,
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    CommentBody(comment = comment, bubbleColor = ModyTheme.colors.gray01, bubbleShape = OthersBubbleShape)
                }
                CommentTime(comment.timeLabel)
            }
        }
    }
}

/** 말풍선 옆 작성 시각. 시안 C4_Medium / Gray4. */
@Composable
private fun CommentTime(label: String?) {
    if (label == null) return
    Text(
        text = label,
        style = ModyTheme.typography.c4,
        color = ModyTheme.colors.gray04,
    )
}

/** 내 댓글 본문. 꾹 누르면 바로 아래에 "삭제" 칩이 뜬다(시안 9:3713). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeletableCommentBody(comment: CommentUi, onDeleteClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box {
        Box(
            // 누름 효과(사각 하이라이트)는 끈다 — 말풍선 모양과 안 맞아 검은 네모가 비친다.
            // 꾹 누르면 진동과 "삭제" 칩이 바로 떠서 피드백은 그걸로 충분하다.
            modifier = Modifier.combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClickLabel = "댓글 메뉴 열기",
                onLongClick = {
                    // 보내는 중인 미리보기는 아직 서버 id 가 없어 지울 수 없다.
                    if (comment.isPending) return@combinedClickable
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuExpanded = true
                },
            ),
        ) {
            CommentBody(comment = comment, bubbleColor = ModyTheme.colors.primary300, bubbleShape = MineBubbleShape)
        }
        // 시안: 말풍선 오른쪽 끝에 맞추고 아래쪽에 4dp 겹친다.
        ModyPopupMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, offsetY = (-4).dp) {
            ModyPopupMenuItem(text = "삭제", onClick = {
                menuExpanded = false
                onDeleteClick()
            })
        }
    }
}

/** 댓글 본문: 이모지 댓글이면 말풍선 없이 이모지 이미지, 글 댓글이면 말풍선. */
@Composable
private fun CommentBody(comment: CommentUi, bubbleColor: Color, bubbleShape: Shape) {
    val emoji = comment.emoji
    if (emoji != null) {
        Image(
            painter = painterResource(emoji.iconRes),
            contentDescription = emoji.description,
            modifier = Modifier.size(EMOJI_COMMENT_SIZE),
        )
    } else {
        val (text, inlineContent) = rememberCommentText(comment)
        Text(
            text = text,
            inlineContent = inlineContent,
            style = ModyTheme.typography.b7,
            color = ModyTheme.colors.gray10,
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/**
 * 말풍선 글 + 사이사이 이모지 그림. 조각이 없으면(구버전 대체 문구 등) 본문을 그대로.
 * 이모지는 글자 한 칸 자리를 차지하는 인라인 그림으로 끼운다(줄높이 22.4 안에 20sp).
 */
@Composable
private fun rememberCommentText(comment: CommentUi): Pair<AnnotatedString, Map<String, InlineTextContent>> =
    remember(comment.segments, comment.content) {
        if (comment.segments.isEmpty()) return@remember AnnotatedString(comment.content) to emptyMap()
        val text = buildAnnotatedString {
            comment.segments.forEach { segment ->
                when (segment) {
                    is CommentSegment.Text -> append(segment.text)
                    is CommentSegment.Emoji -> appendInlineContent(segment.emoji.id, segment.emoji.inputChar)
                }
            }
        }
        val inline = comment.segments.filterIsInstance<CommentSegment.Emoji>().map { it.emoji }.distinct()
            .associate { emoji ->
                emoji.id to InlineTextContent(
                    Placeholder(INLINE_EMOJI_SIZE, INLINE_EMOJI_SIZE, PlaceholderVerticalAlign.TextCenter),
                ) {
                    Image(
                        painter = painterResource(emoji.iconRes),
                        contentDescription = emoji.description,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        text to inline
}

/** 남의 말풍선: 왼쪽 위(아바타 쪽)만 각지고, 왼쪽 아래 16·오른쪽은 완전히 둥글다. */
private val OthersBubbleShape = RoundedCornerShape(topStart = 0.dp, topEnd = 100.dp, bottomEnd = 100.dp, bottomStart = 16.dp)

/** 내 말풍선: 오른쪽 위만 각지고, 오른쪽 아래 16·왼쪽은 완전히 둥글다. */
private val MineBubbleShape = RoundedCornerShape(topStart = 100.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 100.dp)

/** 입력바 위 이모지 줄(시안 "임티" 9:3731). 탭하면 바로 전송된다(글 입력과 무관). */
@Composable
private fun EmojiBar(enabled: Boolean, onClick: (CommentEmoji) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommentEmoji.entries.forEach { emoji ->
            Image(
                painter = painterResource(emoji.iconRes),
                contentDescription = "${emoji.description} 보내기",
                modifier = Modifier
                    .size(EMOJI_BUTTON_SIZE)
                    .clip(CircleShape)
                    .clickable(enabled = enabled) { onClick(emoji) },
            )
        }
    }
}

private val EMOJI_COMMENT_SIZE = 44.dp
private val INLINE_EMOJI_SIZE = 20.sp
private const val PENDING_ALPHA = 0.5f
private val EMOJI_BUTTON_SIZE = 32.dp

/**
 * 댓글 입력바(시안 9:3612). 회색 둥근 입력칸, 글이 있으면 오른쪽에 노란 전송 버튼(44dp)이 붙는다.
 * 이모지 줄과의 간격 8, 좌우 24.
 */
@Composable
private fun CommentInputBar(
    value: TextFieldValue,
    canSend: Boolean,
    onChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = ModyTheme.typography.b7.copy(color = ModyTheme.colors.gray10),
            cursorBrush = SolidColor(ModyTheme.colors.primary100),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(ModyTheme.colors.gray01)
                        .border(1.dp, ModyTheme.colors.gray02, CircleShape)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.text.isEmpty()) {
                        Text(
                            text = "버디에게 응원 한마디를 남겨보세요!",
                            style = ModyTheme.typography.b7,
                            color = ModyTheme.colors.gray04,
                        )
                    }
                    inner()
                }
            },
        )
        if (canSend) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ModyTheme.colors.primary100)
                    .clickable(onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ModyIcons.Send),
                    contentDescription = "전송",
                    tint = ModyTheme.colors.gray10,
                    modifier = Modifier.size(24.dp),
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
