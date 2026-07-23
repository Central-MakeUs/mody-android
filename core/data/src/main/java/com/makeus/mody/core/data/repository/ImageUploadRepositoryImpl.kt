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
        val bytes = readBytes(uri)

        // 1) presigned URL 발급 (imageKey 확장자는 fileName 확장자로 결정)
        val presigned = uploadApi.createPresignedUrl(
            domain = domain,
            fileName = "$fileNameBase.${contentType.toExtension()}",
        ).unwrapResult()

        // 2) S3 직접 업로드
        presignedUploader.upload(presigned.presignedUrl, bytes, contentType)

        return presigned.imageKey
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
