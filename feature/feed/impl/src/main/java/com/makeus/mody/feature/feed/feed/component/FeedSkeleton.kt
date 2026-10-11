package com.makeus.mody.feature.feed.feed.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.modifier.shimmer

/** 피드 로딩 스켈레톤. 개수를 모르므로 고정 [count]장을 shimmer 로 표시. */
@Composable
fun FeedSkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        repeat(count) { FeedCardSkeleton() }
    }
}

/** FeedCard 레이아웃(헤더 + 200dp 이미지)에 맞춘 placeholder. */
@Composable
private fun FeedCardSkeleton() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(32.dp).shimmer(CircleShape))
            Box(modifier = Modifier.width(72.dp).height(14.dp).shimmer())
            Box(modifier = Modifier.width(52.dp).height(24.dp).shimmer(RoundedCornerShape(100.dp)))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shimmer(RoundedCornerShape(16.dp)),
        )
    }
}
