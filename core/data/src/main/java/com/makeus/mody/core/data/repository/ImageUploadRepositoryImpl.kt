package com.makeus.mody.core.data.repository

import android.content.Context
import android.net.Uri
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import com.makeus.mody.core.domain.repository.ImageUploadRepository
import com.makeus.mody.core.network.api.UploadApi
import com.makeus.mody.core.network.model.unwrapResult
import com.makeus.mody.core.network.upload.PresignedUploader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * presigned URL → S3 직접 PUT 3단계 업로드. record/profile 등 공통.
 * (기존 RecordRepositoryImpl 의 인라인 로직을 공통화)
 */
@Singleton
class ImageUploadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uploadApi: UploadApi,
    private val presignedUploader: PresignedUploader,
) : ImageUploadRepository {

    override suspend fun uploadImage(imageUri: String, domain: String, fileNameBase: String): String {
        val uri = Uri.parse(imageUri)
        val contentType = context.contentResolver.getType(uri) ?: DEFAULT_MIME

        // 1) presigned URL 발급 (imageKey 확장자는 fileName 확장자로 결정)
        val presigned = uploadApi.createPresignedUrl(
            domain = domain,
            fileName = "$fileNameBase.${contentType.toExtension()}",
        ).unwrapResult()

        // 2) S3 직접 업로드 — 전체 바이트를 메모리에 올리지 않고 스트리밍(OOM 방지).
        //    S3 PUT 은 Content-Length 필수라 길이를 못 구하는 예외적 URI 만 바이트 폴백.
        val body = streamingBodyOrNull(uri, contentType)
            ?: readBytes(uri).toRequestBody(contentType.toMediaTypeOrNull())
        presignedUploader.upload(presigned.presignedUrl, body)

        return presigned.imageKey
    }

    /** 파일 길이를 알 수 있으면 스트리밍 RequestBody, 아니면 null. */
    private fun streamingBodyOrNull(uri: Uri, contentType: String): RequestBody? {
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

    private suspend fun readBytes(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw HttpResponseException(
                status = HttpResponseStatus.BadRequest,
                errorCode = ModyErrorCode.INVALID_PARAMETER,
                msg = "사진을 불러오지 못했어요. 다시 선택해주세요.",
            )
    }

    private fun String.toExtension(): String = when (this.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg" // image/jpeg 및 그 외는 jpg (서버 허용: jpg/jpeg/png/webp)
    }

    private companion object {
        const val DEFAULT_MIME = "image/jpeg"
    }
}
