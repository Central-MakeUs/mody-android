package com.makeus.mody.core.data.upload

import android.content.Context
import android.net.Uri
import com.makeus.mody.core.model.error.HttpResponseException
import com.makeus.mody.core.model.error.HttpResponseStatus
import com.makeus.mody.core.model.error.ModyErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/**
 * [uri] 의 사진을 presigned PUT 본문으로 만든다.
 *
 * 전체 바이트를 메모리에 올리지 않고 스트리밍한다 — 촬영본을 원본 해상도로 올려 파일이 수 MB 라,
 * ByteArray 로 읽으면 OOM(Error 라 호출부 catch(Exception) 에 안 걸려 앱이 죽는다) 위험이 있다.
 * S3 PUT 은 Content-Length 필수라 길이를 못 구하는 예외적 URI 만 바이트로 읽는다.
 *
 * 파일을 열 수 없으면 여기서 바로 실패한다 — presigned URL 을 받기 전에 걸러야 헛발급이 없다.
 */
internal suspend fun uriUploadBody(context: Context, uri: Uri, contentType: String): RequestBody =
    streamingBodyOrNull(context, uri, contentType)
        ?: readBytes(context, uri).toRequestBody(contentType.toMediaTypeOrNull())

/** 파일 길이를 알 수 있으면 스트리밍 RequestBody, 아니면 null. */
private fun streamingBodyOrNull(context: Context, uri: Uri, contentType: String): RequestBody? {
    val length = runCatching {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
    }.getOrDefault(-1L)
    if (length < 0) return null
    return object : RequestBody() {
        override fun contentType() = contentType.toMediaTypeOrNull()
        override fun contentLength(): Long = length
        override fun writeTo(sink: BufferedSink) {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("사진 스트림을 열지 못했습니다: $uri")
            input.source().use { sink.writeAll(it) }
        }
    }
}

private suspend fun readBytes(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw HttpResponseException(
            status = HttpResponseStatus.BadRequest,
            errorCode = ModyErrorCode.INVALID_PARAMETER,
            msg = "사진을 불러오지 못했어요. 다시 선택해주세요.",
        )
}
