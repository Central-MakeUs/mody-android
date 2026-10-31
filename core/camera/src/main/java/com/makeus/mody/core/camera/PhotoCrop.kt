package com.makeus.mody.core.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

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

/** 정규화된(EXIF 회전 반영) 업라이트 이미지 정보. [uri] 는 표시·업로드용, [path] 는 회전 디코딩·정리용. */
internal data class UprightImage(val uri: String, val path: String, val width: Int, val height: Int)

/**
 * 촬영 원본(EXIF 회전 가능)을 실제 픽셀 방향으로 세워서 다시 저장한다.
 * 이후 크롭 좌표 계산이 화면 표시와 1:1 로 맞도록 회전 애매함을 여기서 제거.
 *
 * 다 쓴 원본 파일은 지운다 — 여기서 만든 결과물만 이후 단계가 참조한다.
 */
internal fun normalizeToUpright(context: Context, sourcePath: String): UprightImage {
    val image = decodeUprightAndSave(context, "capture_${sourcePath.hashCode()}") { FileInputStream(sourcePath) }
    // 원본은 재인코딩된 결과로 대체됐다. 남겨두면 촬영할 때마다 캐시가 두 배로 쌓인다.
    runCatching { File(sourcePath).delete() }
    return image
}

/**
 * 갤러리에서 고른 사진을 촬영본과 같은 형태(업라이트·장변 상한·캐시 JPEG)로 들여온다.
 *
 * 원본 content uri 를 그대로 쓰지 않는 이유: EXIF 회전이 남아 있어 크롭 좌표가 표시와
 * 어긋날 수 있고, 고화소 원본을 그대로 올리면 업로드 시 메모리에 통째로 올라간다.
 * 원본은 사용자 사진이라 지우지 않는다.
 *
 * 파일 이름에 시각을 넣는다 — 같은 사진을 다시 고르면 이름이 겹쳐, 이전 결과를 지울 때
 * 새 결과까지 지워진다.
 */
internal fun importToUpright(context: Context, uri: Uri): UprightImage =
    decodeUprightAndSave(context, "gallery_${uri.hashCode()}_${System.currentTimeMillis()}") {
        context.contentResolver.openInputStream(uri) ?: error("사진을 열 수 없음")
    }

/**
 * [open] 이 주는 이미지를 EXIF 방향대로 세우고, [MAX_UPLOAD_EDGE] 로 줄여 캐시에 저장한다.
 *
 * 디코딩은 축소해서 받는다. 원본 크기로 받으면 메모리가 해상도에 그대로 비례해 터진다.
 * 스트림은 한 번 읽으면 끝이라 EXIF·크기·픽셀을 읽을 때마다 새로 연다.
 */
private fun decodeUprightAndSave(context: Context, key: String, open: () -> InputStream): UprightImage {
    val rotation = readRotationDegrees(open)
    val src = decodeDownsampled(open) ?: error("이미지 디코딩 실패")
    val upright = if (rotation == 0f) {
        src
    } else {
        Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(rotation) }, true)
            .also { if (it != src) src.recycle() }
    }
    return saveJpeg(context, upright, "${key}_${upright.width}x${upright.height}.jpg")
}

/**
 * 조정 화면에서 돌린 방향을 파일에 굽는다.
 *
 * 서버 계약이 "원본 + 정규화 크롭 영역"뿐이라 회전을 따로 실어 보낼 곳이 없다. 그래서
 * 회전된 이미지를 원본으로 올리고, 크롭 영역도 회전된 이미지 기준으로 계산한다(iOS 도 같다).
 * 회전 전 파일은 이 결과로 대체되므로 지운다.
 *
 * @param degrees 시계 방향 90 의 배수.
 */
internal fun rotateUpright(context: Context, image: UprightImage, degrees: Int): UprightImage {
    val src = BitmapFactory.decodeFile(image.path) ?: error("이미지 디코딩 실패")
    val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
    if (rotated != src) src.recycle()
    val result = saveJpeg(context, rotated, "capture_${rotated.width}x${rotated.height}_r${degrees}_${image.path.hashCode()}.jpg")
    runCatching { File(image.path).delete() }
    return result
}

/**
 * [bitmap] 을 캐시에 JPEG 로 저장하고 recycle 한다.
 *
 * 인코딩 실패(저장공간 부족 등)면 결과 파일은 깨진 파일이다. 이때 입력 파일까지 지우면
 * 둘 다 잃으므로, 호출부는 이 함수가 성공한 뒤에만 입력을 지운다.
 */
private fun saveJpeg(context: Context, bitmap: Bitmap, name: String): UprightImage {
    val out = cameraCacheFile(context, name)
    val encoded = runCatching {
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
    }.getOrDefault(false)
    val w = bitmap.width
    val h = bitmap.height
    bitmap.recycle()
    if (!encoded) {
        runCatching { out.delete() }
        error("이미지 인코딩 실패")
    }
    return UprightImage(
        uri = fileUri(context, out).toString(),
        path = out.absolutePath,
        width = w,
        height = h,
    )
}

/** EXIF 회전값(도). 지원하지 않는 값(플립 계열 포함)은 0 으로 둬 기존 동작을 유지한다. */
private fun readRotationDegrees(open: () -> InputStream): Float = when (
    runCatching {
        open().use { ExifInterface(it) }.getAttributeInt(
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
private fun decodeDownsampled(open: () -> InputStream): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open().use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > MAX_UPLOAD_EDGE) sample *= 2

    return open().use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
}

/** 촬영 결과를 받을 임시 원본 파일. */
internal fun createRawFile(context: Context): File =
    cameraCacheFile(context, "raw_${System.currentTimeMillis()}.jpg")

/** 지정 경로의 캐시 파일 삭제(재촬영으로 버려진 결과물 정리). 실패는 무시. */
internal fun deleteCameraFile(path: String) {
    runCatching { File(path).delete() }
}

/**
 * 오래된 촬영 캐시 정리. 오버레이 진입 시 한 번 호출한다.
 *
 * 방금 확정한 사진은 오버레이가 닫힌 뒤에 업로드된다 — 나이 조건 없이 지우면 업로드 중인
 * 파일을 지울 수 있다. [STALE_CACHE_AGE_MS] 보다 오래된 것만 지워 그 창을 피한다.
 */
internal fun clearStaleCameraCache(context: Context) {
    runCatching {
        val threshold = System.currentTimeMillis() - STALE_CACHE_AGE_MS
        cameraCacheDir(context).listFiles()
            ?.filter { it.isFile && it.lastModified() < threshold }
            ?.forEach { it.delete() }
    }
}
