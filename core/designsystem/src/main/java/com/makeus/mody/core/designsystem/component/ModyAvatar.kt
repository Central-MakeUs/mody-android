package com.makeus.mody.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.makeus.mody.core.designsystem.image.ModyImages
import com.makeus.mody.core.designsystem.theme.ModyTheme

@Composable
fun ModyAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    contentDescription: String? = null,
) {
    val defaultAvatar = painterResource(ModyImages.DefaultAvatar)

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        placeholder = defaultAvatar,
        error = defaultAvatar,
        fallback = defaultAvatar,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ModyTheme.colors.gray03),
    )
}
