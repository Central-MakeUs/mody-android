package com.makeus.mody.feature.feed.detail.contract

import androidx.annotation.DrawableRes
import com.makeus.mody.feature.feed.R

/**
 * 응원 이모지. 입력칸이 비어 있으면 탭하는 즉시 이모지만 전송되고, 글을 쓰는 중이면 커서 자리에 들어간다.
 *
 * 서버와는 본문 안의 토큰([token], `:mody-clap:`)으로만 주고받고 이미지는 앱에 내장한다(iOS 도 같은
 * 아이디로 그린다). [id] 는 이미 저장된 댓글이 참조하므로 바꾸지 않는다 — 추가만 한다.
 *
 * @param inputChar 입력칸에서 이 이모지를 대신 보여줄 유니코드 문자. 입력칸(텍스트 필드)은 그림을 끼워 넣을
 *   수 없어, 쓰는 동안은 이 문자로 보이고 보낼 때 [token] 으로 바뀐다([CommentEmojiCodec.encode]).
 */
enum class CommentEmoji(
    val id: String,
    @DrawableRes val iconRes: Int,
    val description: String,
    val inputChar: String,
) {
    CLAP("clap", R.drawable.ic_emoji_clap, "박수", "👏"), // 👏
    MEDAL("medal", R.drawable.ic_emoji_medal, "메달", "🏅"), // 🏅
    FIRE("fire", R.drawable.ic_emoji_fire, "불꽃", "🔥"), // 🔥
    HEART("heart", R.drawable.ic_emoji_heart, "하트", "💛"), // 💛
    RUN("run", R.drawable.ic_emoji_run, "달리기", "🏃"), // 🏃
    ;

    /** 서버 본문 안의 표기. */
    val token: String get() = "$TOKEN_PREFIX$id$TOKEN_SUFFIX"

    companion object {
        const val TOKEN_PREFIX = ":mody-"
        const val TOKEN_SUFFIX = ":"

        /** 모르는 아이디(서버가 새 이모지를 추가했는데 이 앱이 구버전인 경우)는 null. */
        fun of(id: String?): CommentEmoji? = entries.firstOrNull { it.id == id }
    }
}
