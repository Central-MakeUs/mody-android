package com.makeus.mody.core.network.upload

import com.makeus.mody.core.domain.error.ErrorReporter
import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * presigned URL 로 이미지 바이트를 직접 PUT 업로드 (S3).
 * 인증은 URL 쿼리 서명에 이미 포함되므로 Authorization 헤더를 붙이면 안 된다.
 * → [AuthInterceptor] 가 API 호스트가 아닌 요청엔 토큰을 붙이지 않도록 처리돼 있다.
 */
@Singleton
class PresignedUploader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val errorReporter: ErrorReporter,
) {
    /**
     * 업로드 전용 클라이언트. 공유 클라이언트의 API 용 상한(callTimeout 30s)으로는
     * 느린 회선에서 사진 한 장도 못 올린다.
     * [OkHttpClient.newBuilder] 는 커넥션 풀·디스패처를 그대로 공유하므로 추가 비용이 없다.
     */
    private val uploadClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .writeTimeout(UPLOAD_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(UPLOAD_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    suspend fun upload(presignedUrl: String, bytes: ByteArray, contentType: String) =
        upload(presignedUrl, bytes.toRequestBody(contentType.toMediaTypeOrNull()))

    /** 대용량 파일은 [body] 를 스트리밍 RequestBody 로 넘겨 메모리 버퍼링 없이 업로드. */
    suspend fun upload(presignedUrl: String, body: RequestBody) {
        val request = Request.Builder()
            .url(presignedUrl)
            .put(body)
            .build()

        withContext(Dispatchers.IO) {
            uploadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val e = HttpResponseException(
                        status = HttpResponseStatus.create(response.code),
                        errorCode = ModyErrorCode.UNKNOWN,
                        msg = "사진 업로드에 실패했어요. 잠시 후 다시 시도해주세요.",
                    )
                    // S3 직통이라 Retrofit CallAdapter 를 안 탄다 — 여기서 직접 보고한다.
                    // presigned URL 은 쿼리에 서명이 들어 있어 통째로 남기면 안 된다.
                    errorReporter.report(
                        throwable = e,
                        context = mapOf(
                            "source" to "presigned_upload",
                            "http_status" to response.code.toString(),
                        ),
                    )
                    throw e
                }
            }
        }
    }

    private companion object {
        const val UPLOAD_WRITE_TIMEOUT_SECONDS = 60L
        const val UPLOAD_CALL_TIMEOUT_SECONDS = 120L
    }
}
