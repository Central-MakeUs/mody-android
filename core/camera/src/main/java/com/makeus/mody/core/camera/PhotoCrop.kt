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
 * 업로드본 JPEG 품질.
 *
 * 95 → 88 로 내리면 용량이 크게 줄고, 표시 크기(1200px 남짓)로 줄여 그리면 차이가 보이지 않는다.
 * 대신 이 값은 인코딩이 한 번뿐이라는 전제다 — 여러 번 재인코딩하면 손실이 쌓인다.
 * iOS 는 기록 사진을 원본 그대로(회전 시 0.95) 올린다. 인코더가 달라 숫자를 1:1 로 맞출 수는 없다.
 */
private const val UPLOAD_JPEG_QUALITY = 88

/**
 * 업로드본을 원본 해상도로 만들다 메모리가 모자랄 때 물러설 최대 샘플링 배율.
 * 1/2, 1/4 순서로 줄여 다시 시도하고, 여기서도 안 되면 실패로 넘긴다.
 */
private const val UPLOAD_MAX_FALLBACK_SAMPLE = 4

/**
 * 조정 화면 표시용 사본의 장변 상한(px). 업로드되지 않는다.
 *
 * 원본을 그대로 비트맵으로 올리면 ARGB_8888 기준 12MP 가 약 48MB, 50MP 면 약 200MB 라
 * 표시용은 따로 줄여 둔다.
 */
private const val PREVIEW_MAX_EDGE = 2048

/** 표시용 사본 품질. 업로드와 무관해 화면에서 티 나지 않을 만큼만. */
private const val PREVIEW_JPEG_QUALITY = 90

/** 진입 시 지울 캐시 파일의 최소 나이(ms). 업로드가 아직 참조 중인 파일을 지우지 않도록 여유를 둔다. */
private const val STALE_CACHE_AGE_MS = 10 * 60 * 1000L

/** 촬영/크롭 결과를 담는 캐시 파일. camera_file_paths.xml 의 cache-path(camera)와 일치. */
private fun cameraCacheDir(context: Context): File =
    File(context.cacheDir, "camera").apply { mkdirs() }

private fun cameraCacheFile(context: Context, name: String): File =
    File(cameraCacheDir(context), name)

private fun fileUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.camera.fileprovider", file)

/** EXIF 회전을 반영해 세운 이미지 파일. [uri] 는 표시·업로드용, [path] 는 정리용. */
internal data class UprightImage(val uri: String, val path: String, val width: Int, val height: Int)

/**
 * 조정 화면에서 다루는 사진.
 *
 * 업로드본은 확정할 때 [source] 에서 한 번만 만든다([exportForUpload]). 미리 세워 저장한 파일에서
 * 다시 만들면 촬영 → 정규화 → 회전마다 JPEG 를 다시 인코딩해 화질이 깎인다.
 *
 * @param preview 화면 표시·크롭 좌표 계산용 사본(EXIF 회전 반영). 비율만 원본과 같으면 되고
 *   크롭 영역은 정규화 비율이라 업로드본 크기와 무관하다.
 * @param source 원본. 촬영본은 캐시 파일, 갤러리 사진은 선택기가 준 content uri.
 * @param ownedSourcePath 다 쓰면 지울 원본 경로. 촬영본만 있다 — 갤러리 원본은 사용자 사진이라 지우지 않는다.
 */
internal data class EditableImage(
    val preview: UprightImage,
    val source: Uri,
    val ownedSourcePath: String?,
)

/**
 * 촬영 원본으로 조정 화면을 준비한다. 원본은 업로드본을 만들 때까지 남겨 둔다.
 *
 * 표시용 사본은 실제 픽셀 방향으로 세워 둔다 — 크롭 좌표 계산이 화면 표시와 1:1 로 맞도록
 * EXIF 회전 애매함을 여기서 없앤다.
 */
internal fun prepareCaptured(context: Context, rawPath: String): EditableImage =
    EditableImage(
        preview = decodePreview(context, "capture_${rawPath.hashCode()}") { FileInputStream(rawPath) },
        source = Uri.fromFile(File(rawPath)),
        ownedSourcePath = rawPath,
    )

/**
 * 갤러리에서 고른 사진으로 조정 화면을 준비한다.
 *
 * 파일 이름에 시각을 넣는다 — 같은 사진을 다시 고르면 이름이 겹쳐, 이전 사본을 지울 때
 * 새 사본까지 지워진다.
 */
internal fun prepareGallery(context: Context, uri: Uri): EditableImage =
    EditableImage(
        preview = decodePreview(context, "gallery_${uri.hashCode()}_${System.currentTimeMillis()}") {
            context.contentResolver.openInputStream(uri) ?: error("사진을 열 수 없음")
        },
        source = uri,
        ownedSourcePath = null,
    )

/** 조정 화면에서 쓰던 파일(표시용 사본, 촬영 원본) 정리. 갤러리 원본은 건드리지 않는다. */
internal fun discardEditable(image: EditableImage) {
    deleteCameraFile(image.preview.path)
    image.ownedSourcePath?.let(::deleteCameraFile)
}

/**
 * 원본에서 업로드본을 만든다. 원본 해상도 그대로, EXIF 회전·조정 화면 회전을 한 번에 적용하고
 * 한 번만 인코딩한다. 확대해서 좁게 잘라도 화질이 남도록 줄이지 않는다(iOS 도 원본을 올린다).
 *
 * 서버 계약이 "원본 + 정규화 크롭 영역"뿐이라 회전을 따로 실어 보낼 곳이 없다. 그래서
 * 회전된 이미지를 원본으로 올리고, 크롭 영역도 회전된 이미지 기준으로 계산한다(iOS 도 같다).
 *
 * 메모리: 원본과 회전본이 잠깐 함께 있다(12MP 면 약 96MB, 50MP 면 약 400MB). API 26+ 는 비트맵
 * 픽셀이 네이티브 힙이라 Java 힙 한도엔 안 걸리지만 기기 메모리가 모자랄 수 있어, 할당에
 * 실패하면 1/2, 1/4 로 줄여 다시 시도한다.
 *
 * @param quarterTurns 조정 화면에서 돌린 시계 방향 90° 횟수(0~3).
 */
internal fun exportForUpload(context: Context, image: EditableImage, quarterTurns: Int): UprightImage {
    val open = { context.contentResolver.openInputStream(image.source) ?: error("사진을 열 수 없음") }
    val degrees = (readRotationDegrees(open) + quarterTurns * 90) % 360
    var sample = 1
    while (true) {
        try {
            return encodeUpload(context, open, degrees, sample)
        } catch (_: OutOfMemoryError) {
            // Error 는 호출부의 catch(Exception) 에 안 걸려 앱이 죽는다 — 마지막엔 예외로 바꿔 넘긴다.
            if (sample >= UPLOAD_MAX_FALLBACK_SAMPLE) error("메모리 부족으로 사진을 저장하지 못함")
            sample *= 2
        }
    }
}

private fun encodeUpload(context: Context, open: () -> InputStream, degrees: Int, sample: Int): UprightImage {
    val src = open().use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
        ?: error("이미지 디코딩 실패")
    val result = if (degrees == 0) {
        src
    } else {
        try {
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
        } finally {
            // 회전본 할당에 실패해도 원본은 놓아야 재시도할 메모리가 생긴다.
            src.recycle()
        }
    }
    return saveJpeg(
        context,
        result,
        "upload_${System.currentTimeMillis()}_${result.width}x${result.height}.jpg",
        UPLOAD_JPEG_QUALITY,
    )
}

/**
 * [open] 이 주는 이미지를 EXIF 방향대로 세우고 [PREVIEW_MAX_EDGE] 이하로 줄여 캐시에 저장한다.
 *
 * 디코딩은 축소해서 받는다. 원본 크기로 받으면 메모리가 해상도에 그대로 비례해 터진다.
 * 표시용이라 2의 거듭제곱 샘플링만 한다(목표보다 작아질 수 있지만 화면 폭보다는 크다).
 * 스트림은 한 번 읽으면 끝이라 EXIF·크기·픽셀을 읽을 때마다 새로 연다.
 */
private fun decodePreview(context: Context, key: String, open: () -> InputStream): UprightImage {
    val rotation = readRotationDegrees(open)
    val src = decodeDownsampled(open) ?: error("이미지 디코딩 실패")
    val upright = if (rotation == 0) {
        src
    } else {
        Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(rotation.toFloat()) }, true)
            .also { if (it != src) src.recycle() }
    }
    return saveJpeg(context, upright, "${key}_${upright.width}x${upright.height}.jpg", PREVIEW_JPEG_QUALITY)
}

/**
 * [bitmap] 을 캐시에 JPEG 로 저장하고 recycle 한다.
 *
 * 인코딩 실패(저장공간 부족 등)면 결과 파일은 깨진 파일이다. 이때 입력 파일까지 지우면
 * 둘 다 잃으므로, 호출부는 이 함수가 성공한 뒤에만 입력을 지운다.
 */
private fun saveJpeg(context: Context, bitmap: Bitmap, name: String, quality: Int): UprightImage {
    val out = cameraCacheFile(context, name)
    val encoded = runCatching {
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
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
private fun readRotationDegrees(open: () -> InputStream): Int = when (
    runCatching {
        open().use { ExifInterface(it) }.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
) {
    ExifInterface.ORIENTATION_ROTATE_90 -> 90
    ExifInterface.ORIENTATION_ROTATE_180 -> 180
    ExifInterface.ORIENTATION_ROTATE_270 -> 270
    else -> 0
}

/**
 * 장변이 [PREVIEW_MAX_EDGE] 이하가 되도록 축소해 디코딩한다.
 *
 * 먼저 [BitmapFactory.Options.inJustDecodeBounds] 로 크기만 읽어(픽셀 할당 없음)
 * 샘플링 배율을 정한다.
 */
private fun decodeDownsampled(open: () -> InputStream): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open().use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > PREVIEW_MAX_EDGE) sample *= 2

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
