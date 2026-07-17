package com.makeus.mody.core.data.repository

import android.content.Context
import android.net.Uri
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import com.makeus.mody.core.domain.repository.RecordRepository
import com.makeus.mody.core.network.api.RecordApi
import com.makeus.mody.core.network.model.record.RecordCreateRequest
import com.makeus.mody.core.network.model.unwrapResult
import com.makeus.mody.core.network.upload.PresignedUploader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordApi: RecordApi,
    private val presignedUploader: PresignedUploader,
) : RecordRepository {

    override suspend fun createMealRecord(
        imageUri: String,
        menu: String,
        mealTime: LocalTime,
    ): Long {
        val uri = Uri.parse(imageUri)
        val contentType = context.contentResolver.getType(uri) ?: DEFAULT_MIME
        val bytes = readBytes(uri)

        // 1) presigned URL 발급 (imageKey 확장자는 fileName 확장자로 결정)
        val presigned = recordApi.createPresignedUrl(
            domain = UPLOAD_DOMAIN,
            fileName = "meal.${contentType.toExtension()}",
        ).unwrapResult()

        // 2) S3 직접 업로드
        presignedUploader.upload(presigned.presignedUrl, bytes, contentType)

        // 3) 기록 생성
        return recordApi.createRecord(
            RecordCreateRequest(
                recordType = "MEAL",
                imageKey = presigned.imageKey,
                mealTime = mealTime.format(TIME_FORMATTER),
                menu = menu,
            ),
        ).unwrapResult().recordId
    }

    override suspend fun createExerciseRecord(
        imageUri: String,
        exerciseName: String,
        durationHours: Int,
        durationMinutes: Int,
    ): Long {
        val uri = Uri.parse(imageUri)
        val contentType = context.contentResolver.getType(uri) ?: DEFAULT_MIME
        val bytes = readBytes(uri)

        // 1) presigned URL 발급
        val presigned = recordApi.createPresignedUrl(
            domain = UPLOAD_DOMAIN,
            fileName = "exercise.${contentType.toExtension()}",
        ).unwrapResult()

        // 2) S3 직접 업로드
        presignedUploader.upload(presigned.presignedUrl, bytes, contentType)

        // 3) 기록 생성
        return recordApi.createRecord(
            RecordCreateRequest(
                recordType = "EXERCISE",
                imageKey = presigned.imageKey,
                exerciseName = exerciseName,
                exerciseDurationHours = durationHours,
                exerciseDurationMinutes = durationMinutes,
            ),
        ).unwrapResult().recordId
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
        else -> "jpg" // image/jpeg 및 그 외는 jpg 로 취급 (서버 허용: jpg/jpeg/png/webp)
    }

    private companion object {
        const val UPLOAD_DOMAIN = "record"
        const val DEFAULT_MIME = "image/jpeg"
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
