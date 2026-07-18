package com.makeus.mody.feature.record.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** 촬영/크롭 결과를 담는 캐시 파일. record_file_paths.xml 의 cache-path(record/camera)와 일치. */
private fun cameraCacheFile(context: Context, name: String): File {
    val dir = File(context.cacheDir, "record/camera").apply { mkdirs() }
    return File(dir, name)
}

private fun fileUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.record.fileprovider", file)

/** 정규화된(EXIF 회전 반영) 업라이트 이미지 정보. [uri] 는 표시용, [path] 는 크롭 디코딩용. */
data class UprightImage(val uri: String, val path: String, val width: Int, val height: Int)

/**
 * 촬영 원본(EXIF 회전 가능)을 실제 픽셀 방향으로 세워서 다시 저장한다.
 * 이후 크롭 좌표 계산이 화면 표시와 1:1 로 맞도록 회전 애매함을 여기서 제거.
 */
fun normalizeToUpright(context: Context, sourcePath: String): UprightImage {
    val src = BitmapFactory.decodeFile(sourcePath) ?: error("이미지 디코딩 실패")
    val rotation = when (
        ExifInterface(sourcePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    val upright = if (rotation == 0f) {
        src
    } else {
        Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(rotation) }, true)
            .also { if (it != src) src.recycle() }
    }
    val out = cameraCacheFile(context, "capture_${upright.width}x${upright.height}_${sourcePath.hashCode()}.jpg")
    FileOutputStream(out).use { upright.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    val w = upright.width
    val h = upright.height
    upright.recycle()
    return UprightImage(
        uri = fileUri(context, out).toString(),
        path = out.absolutePath,
        width = w,
        height = h,
    )
}

/**
 * 업라이트 이미지에서 [sourceTop] 부터 세로 [sourceHeight] px(가로 전체)을 잘라 새 파일로 저장.
 * 좌표는 업라이트 픽셀 기준. 반환은 결과 파일 uri 문자열.
 */
fun cropVertical(context: Context, uprightPath: String, sourceTop: Int, sourceHeight: Int): String {
    val bmp = BitmapFactory.decodeFile(uprightPath) ?: error("이미지 디코딩 실패")
    val top = sourceTop.coerceIn(0, (bmp.height - 1).coerceAtLeast(0))
    val height = sourceHeight.coerceIn(1, bmp.height - top)
    val cropped = Bitmap.createBitmap(bmp, 0, top, bmp.width, height)
    if (cropped != bmp) bmp.recycle()
    val out = cameraCacheFile(context, "crop_${System.currentTimeMillis()}.jpg")
    FileOutputStream(out).use { cropped.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    cropped.recycle()
    return fileUri(context, out).toString()
}

/** 촬영 결과를 받을 임시 원본 파일. */
fun createRawFile(context: Context): File =
    cameraCacheFile(context, "raw_${System.currentTimeMillis()}.jpg")
