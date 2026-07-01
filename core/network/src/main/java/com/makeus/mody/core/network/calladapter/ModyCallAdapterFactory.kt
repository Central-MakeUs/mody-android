package com.makeus.mody.core.network.calladapter

import com.makeus.mody.core.domain.model.error.HttpResponseException
import com.makeus.mody.core.domain.model.error.HttpResponseStatus
import com.makeus.mody.core.domain.model.error.ModyErrorCode
import com.makeus.mody.core.network.model.ApiErrorResponse
import com.makeus.mody.core.network.model.ApiResponse
import kotlinx.serialization.json.Json
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * 반환타입이 `ApiResponse<T>` 인 Retrofit 호출을 가로채:
 * - 2xx  → 그대로 전달
 * - 그 외 → errorBody 를 [ApiErrorResponse] 로 파싱해 [HttpResponseException] 으로 던짐
 *
 * 덕분에 DataSource 는 성공 응답만 다루고, 에러는 예외로 일원화된다.
 */
class ModyCallAdapterFactory @Inject constructor(
    private val json: Json,
) : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null

        val callType = returnType as ParameterizedType
        val innerType = getParameterUpperBound(0, callType)
        if (getRawType(innerType) != ApiResponse::class.java) return null

        return ModyCallAdapter<Any>(innerType, json)
    }
}

private class ModyCallAdapter<T : Any>(
    private val resultType: Type,
    private val json: Json,
) : CallAdapter<T, Call<T>> {
    override fun responseType(): Type = resultType
    override fun adapt(call: Call<T>): Call<T> = ErrorHandlingCall(call, json)
}

private class ErrorHandlingCall<T : Any>(
    private val delegate: Call<T>,
    private val json: Json,
) : Call<T> {
    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    callback.onResponse(this@ErrorHandlingCall, response)
                    return
                }

                val errorBody = response.errorBody()?.string()
                val errorResponse = runCatching {
                    json.decodeFromString(ApiErrorResponse.serializer(), errorBody ?: "")
                }.getOrNull()

                val exception = HttpResponseException(
                    status = HttpResponseStatus.create(response.code()),
                    errorCode = ModyErrorCode.create(errorResponse?.code),
                    msg = errorResponse?.errors?.firstOrNull()?.message
                        ?: errorResponse?.message
                        ?: "일시적인 서버 오류입니다. 반복되면 문의해주세요.",
                )
                callback.onFailure(this@ErrorHandlingCall, exception)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(this@ErrorHandlingCall, t)
            }
        })
    }

    override fun clone(): Call<T> = ErrorHandlingCall(delegate.clone(), json)
    override fun execute(): Response<T> =
        throw UnsupportedOperationException("suspend/enqueue 만 지원")

    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
    override fun cancel() = delegate.cancel()
}
