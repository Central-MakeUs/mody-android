package com.makeus.mody.core.network.upload

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
) {
    suspend fun upload(presignedUrl: String, bytes: ByteArray, contentType: String) =
        upload(presignedUrl, bytes.toRequestBody(contentType.toMediaTypeOrNull()))

    /** 대용량 파일은 [body] 를 스트리밍 RequestBody 로 넘겨 메모리 버퍼링 없이 업로드. */
    suspend fun upload(presignedUrl: String, body: RequestBody) {
        val request = Request.Builder()
            .url(presignedUrl)
            .put(body)
            .build()

        withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpResponseException(
                        status = HttpResponseStatus.create(response.code),
                        errorCode = ModyErrorCode.UNKNOWN,
                        msg = "사진 업로드에 실패했어요. 잠시 후 다시 시도해주세요.",
                    )
                }
            }
        }
    }
}
