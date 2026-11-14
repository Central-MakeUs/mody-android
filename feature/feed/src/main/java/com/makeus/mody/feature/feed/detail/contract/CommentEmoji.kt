package com.makeus.mody.feature.feed.detail.contract

import androidx.annotation.DrawableRes
import com.makeus.mody.feature.feed.R

/**
 * 이모지 댓글 종류. 탭하면 글 입력 없이 바로 전송된다.
 *
 * 서버와는 [id] 만 주고받고, 이미지는 앱에 내장한다(iOS 도 같은 아이디로 그린다).
 * [id] 는 이미 저장된 댓글이 참조하므로 바꾸지 않는다 — 추가만 한다.
 */
enum class CommentEmoji(val id: String, @DrawableRes val iconRes: Int, val description: String) {
    CLAP("clap", R.drawable.ic_emoji_clap, "박수"),
    MEDAL("medal", R.drawable.ic_emoji_medal, "메달"),
    FIRE("fire", R.drawable.ic_emoji_fire, "불꽃"),
    HEART("heart", R.drawable.ic_emoji_heart, "하트"),
    RUN("run", R.drawable.ic_emoji_run, "달리기"),
    ;

    companion object {
        /** 모르는 아이디(서버가 새 이모지를 추가했는데 이 앱이 구버전인 경우)는 null. */
        fun of(id: String?): CommentEmoji? = entries.firstOrNull { it.id == id }
    }
}
