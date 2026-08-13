package com.makeus.mody.core.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * 업로드본 장변 상한(px).
 *
 * 캡처는 센서 최고 해상도로 받지만(구도·화질), 그 크기를 그대로 비트맵으로 올리면
 * ARGB_8888 기준 12MP 가 약 48MB, 50MP 면 약 200MB 다. 회전 시에는 원본과 회전본이
 * 잠깐 함께 존재해 두 배가 된다 — 고화소 기기에서 OOM 이 난다.
 *
 * 표시 최대 폭이 3x 기기에서도 1200px 남짓이라 2048 이면 확대·재크롭 여지까지 충분하다.
 */
private const val MAX_UPLOAD_EDGE = 2048

/** 재인코딩 품질. 축소 후 저장이라 파일 크기보다 화질 보존 쪽에 둔다. */
private const val JPEG_QUALITY = 95

/** 진입 시 지울 캐시 파일의 최소 나이(ms). 업로드가 아직 참조 중인 파일을 지우지 않도록 여유를 둔다. */
private const val STALE_CACHE_AGE_MS = 10 * 60 * 1000L

/** 촬영/크롭 결과를 담는 캐시 파일. camera_file_paths.xml 의 cache-path(camera)와 일치. */
private fun cameraCacheDir(context: Context): File =
    File(context.cacheDir, "camera").apply { mkdirs() }

private fun cameraCacheFile(context: Context, name: String): File =
    File(cameraCacheDir(context), name)

private fun fileUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.camera.fileprovider", file)

/** 정규화된(EXIF 회전 반영) 업라이트 이미지 정보. [uri] 는 표시용, [path] 는 크롭 디코딩용. */
data class UprightImage(val uri: String, val path: String, val width: Int, val height: Int)

/**
 * 촬영 원본(EXIF 회전 가능)을 실제 픽셀 방향으로 세워서 다시 저장한다.
 * 이후 크롭 좌표 계산이 화면 표시와 1:1 로 맞도록 회전 애매함을 여기서 제거.
 *
 * 디코딩은 [MAX_UPLOAD_EDGE] 에 맞춰 축소해서 받는다. 원본 크기로 받으면 메모리가
 * 해상도에 그대로 비례해 터진다.
 *
 * 다 쓴 원본 파일은 지운다 — 여기서 만든 결과물만 이후 단계가 참조한다.
 */
fun normalizeToUpright(context: Context, sourcePath: String): UprightImage {
    val rotation = readRotationDegrees(sourcePath)
    val src = decodeDownsampled(sourcePath) ?: error("이미지 디코딩 실패")
    val upright = if (rotation == 0f) {
        src
    } else {
        Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(rotation) }, true)
            .also { if (it != src) src.recycle() }
    }
    val out = cameraCacheFile(context, "capture_${upright.width}x${upright.height}_${sourcePath.hashCode()}.jpg")
    FileOutputStream(out).use { upright.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
    val w = upright.width
    val h = upright.height
    upright.recycle()
    // 원본은 재인코딩된 결과로 대체됐다. 남겨두면 촬영할 때마다 캐시가 두 배로 쌓인다.
    runCatching { File(sourcePath).delete() }
    return UprightImage(
        uri = fileUri(context, out).toString(),
        path = out.absolutePath,
        width = w,
        height = h,
    )
}

/** EXIF 회전값(도). 지원하지 않는 값(플립 계열 포함)은 0 으로 둬 기존 동작을 유지한다. */
private fun readRotationDegrees(sourcePath: String): Float = when (
    runCatching {
        ExifInterface(sourcePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
) {
    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
    else -> 0f
}

/**
 * 장변이 [MAX_UPLOAD_EDGE] 이하가 되도록 축소해 디코딩한다.
 *
 * 먼저 [BitmapFactory.Options.inJustDecodeBounds] 로 크기만 읽어(픽셀 할당 없음)
 * 샘플링 배율을 정한다. inSampleSize 는 2의 거듭제곱만 적용돼 결과가 상한보다 클 수
 * 있지만, 최소 절반씩 줄어 메모리 상한을 잡는 목적에는 충분하다.
 */
private fun decodeDownsampled(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > MAX_UPLOAD_EDGE) sample *= 2

    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}

/** 촬영 결과를 받을 임시 원본 파일. */
fun createRawFile(context: Context): File =
    cameraCacheFile(context, "raw_${System.currentTimeMillis()}.jpg")

/** 지정 경로의 캐시 파일 삭제(재촬영으로 버려진 결과물 정리). 실패는 무시. */
fun deleteCameraFile(path: String) {
    runCatching { File(path).delete() }
}

/**
 * 오래된 촬영 캐시 정리. 오버레이 진입 시 한 번 호출한다.
 *
 * 방금 확정한 사진은 오버레이가 닫힌 뒤에 업로드된다 — 나이 조건 없이 지우면 업로드 중인
 * 파일을 지울 수 있다. [STALE_CACHE_AGE_MS] 보다 오래된 것만 지워 그 창을 피한다.
 */
fun clearStaleCameraCache(context: Context) {
    runCatching {
        val threshold = System.currentTimeMillis() - STALE_CACHE_AGE_MS
        cameraCacheDir(context).listFiles()
            ?.filter { it.isFile && it.lastModified() < threshold }
            ?.forEach { it.delete() }
    }
}
