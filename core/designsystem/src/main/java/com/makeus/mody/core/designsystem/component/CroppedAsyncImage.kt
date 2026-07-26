package com.makeus.mody.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.roundToInt

/**
 * 원본 이미지를 로드하되, [cropX]/[cropY]/[cropWidth]/[cropHeight](정규화 0~1)가 모두 주어지면
 * 디코드 시 해당 영역만 잘라 표시한다. 크롭 값이 하나라도 null 이면 원본 전체를 그대로 표시.
 *
 * 서버가 원본을 저장하고 표시 영역(imageCropRegion)만 내려주는 방식에 대응.
 * 크롭은 Coil Transformation 으로 decode 단계에서 처리 → 표시는 ContentScale.Crop 로 박스 채움.
 */
@Composable
fun CroppedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cropX: Float? = null,
    cropY: Float? = null,
    cropWidth: Float? = null,
    cropHeight: Float? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(model)
        .apply {
            if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
                transformations(CropRegionTransformation(cropX, cropY, cropWidth, cropHeight))
            }
        }
        .build()

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

/** 정규화(0~1) 사각 영역을 원본 비트맵에서 잘라내는 Coil 변환. */
private class CropRegionTransformation(
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float,
) : Transformation {

    override val cacheKey: String = "crop-$x-$y-$width-$height"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val iw = input.width
        val ih = input.height
        val left = (x * iw).roundToInt().coerceIn(0, iw - 1)
        val top = (y * ih).roundToInt().coerceIn(0, ih - 1)
        val w = (width * iw).roundToInt().coerceIn(1, iw - left)
        val h = (height * ih).roundToInt().coerceIn(1, ih - top)
        return Bitmap.createBitmap(input, left, top, w, h)
    }
}
