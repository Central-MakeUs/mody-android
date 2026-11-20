package com.makeus.mody.feature.feed.detail.contract

import com.makeus.mody.feature.feed.detail.contract.CommentSegment.Emoji
import com.makeus.mody.feature.feed.detail.contract.CommentSegment.Text
import org.junit.Assert.assertEquals
import org.junit.Test

/** 서버 계약: 본문 안 `:mody-<id>:` 토큰. 아는 토큰만 그림, 모르는 토큰은 글자 그대로. */
class CommentEmojiCodecTest {

    @Test
    fun `입력칸 대체 문자는 보낼 때 토큰으로 바뀐다`() {
        val input = "오늘도 ${CommentEmoji.FIRE.inputChar} 화이팅 ${CommentEmoji.CLAP.inputChar}"
        assertEquals("오늘도 :mody-fire: 화이팅 :mody-clap:", CommentEmojiCodec.encode(input))
    }

    @Test
    fun `이모지만 있으면 토큰 하나가 된다`() {
        assertEquals(":mody-heart:", CommentEmojiCodec.encode(CommentEmoji.HEART.inputChar))
    }

    @Test
    fun `글과 토큰을 조각으로 나눈다`() {
        assertEquals(
            listOf(Text("오늘도 "), Emoji(CommentEmoji.FIRE), Text(" 화이팅 "), Emoji(CommentEmoji.CLAP)),
            CommentEmojiCodec.parse("오늘도 :mody-fire: 화이팅 :mody-clap:"),
        )
    }

    @Test
    fun `모르는 토큰은 글자 그대로 앞뒤 글과 합친다`() {
        assertEquals(
            listOf(Text("짱 :mody-star: 이다"), Emoji(CommentEmoji.RUN)),
            CommentEmojiCodec.parse("짱 :mody-star: 이다:mody-run:"),
        )
    }

    @Test
    fun `토큰이 붙어 있어도 각각 그린다`() {
        assertEquals(
            listOf(Emoji(CommentEmoji.HEART), Emoji(CommentEmoji.HEART)),
            CommentEmojiCodec.parse(":mody-heart::mody-heart:"),
        )
    }

    @Test
    fun `토큰 형식이 아니면 건드리지 않는다`() {
        assertEquals(listOf(Text("시간 10:30 :clap: :mody-CLAP:")), CommentEmojiCodec.parse("시간 10:30 :clap: :mody-CLAP:"))
    }

    @Test
    fun `보내고 받으면 같은 이모지가 돌아온다`() {
        CommentEmoji.entries.forEach { emoji ->
            assertEquals(listOf(Emoji(emoji)), CommentEmojiCodec.parse(CommentEmojiCodec.encode(emoji.inputChar)))
        }
    }
}
