package com.makeus.mody.feature.feed.detail.contract

/** 댓글 본문 조각. 화면은 [Text] 는 글자로, [Emoji] 는 그림으로 그린다. */
sealed interface CommentSegment {
    data class Text(val text: String) : CommentSegment
    data class Emoji(val emoji: CommentEmoji) : CommentSegment
}

/**
 * 댓글 본문 ↔ 이모지 토큰 변환(서버 계약: 본문 안 `:mody-<id>:`).
 *
 * - 보낼 때: 입력칸의 대체 문자([CommentEmoji.inputChar])를 토큰으로 바꾼다.
 * - 받을 때: 아는 토큰은 이모지 조각으로, 모르는 토큰은 서버처럼 글자 그대로 둔다.
 */
object CommentEmojiCodec {

    private val TOKEN = Regex(":mody-([a-z]+):")

    /** 입력칸 글 → 서버 본문. */
    fun encode(input: String): String =
        CommentEmoji.entries.fold(input) { text, emoji -> text.replace(emoji.inputChar, emoji.token) }

    /** 서버 본문 → 조각들. 이웃한 글자 조각은 하나로 합친다. */
    fun parse(content: String): List<CommentSegment> {
        val segments = mutableListOf<CommentSegment>()
        var last = 0
        fun appendText(text: String) {
            if (text.isEmpty()) return
            val prev = segments.lastOrNull()
            if (prev is CommentSegment.Text) {
                segments[segments.lastIndex] = CommentSegment.Text(prev.text + text)
            } else {
                segments += CommentSegment.Text(text)
            }
        }
        for (match in TOKEN.findAll(content)) {
            appendText(content.substring(last, match.range.first))
            val emoji = CommentEmoji.of(match.groupValues[1])
            if (emoji != null) segments += CommentSegment.Emoji(emoji) else appendText(match.value)
            last = match.range.last + 1
        }
        appendText(content.substring(last))
        return segments
    }
}
