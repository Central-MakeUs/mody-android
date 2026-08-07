package com.makeus.mody.core.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import com.makeus.mody.core.domain.repository.ImageShareRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * 공유용 이미지 다운로드. 앱 캐시(`cacheDir/share`)에 저장하고 FileProvider URI 로 노출한다.
 *
 * 인증 토큰이 필요 없는 공개 URL(S3/CDN)을 받으므로 인터셉터가 붙은 공용 OkHttpClient 대신
 * 별도 클라이언트를 쓴다 — Authorization 헤더를 실어 보내면 S3 서명과 충돌한다.
 */
@Singleton
class ImageShareRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageShareRepository {

    /**
     * callTimeout 은 호출 전체(DNS·연결·본문 수신)의 상한이다. 기본값인 readTimeout 만으로는
     * 한 번의 read 가 막히는 것만 막아서, 응답이 아주 느리게 계속 흘러오면 다운로드가 끝나지
     * 않고 공유 버튼이 로딩 상태로 잠긴다.
     */
    private val client by lazy {
        OkHttpClient.Builder()
            .callTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun downloadForSharing(
        imageUrl: String,
        fileNameBase: String,
    ): String = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
        val file = File(dir, "$fileNameBase.${imageUrl.toExtension()}")
        client.newCall(Request.Builder().url(imageUrl).build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("공유 이미지 다운로드 실패 (HTTP ${response.code})")
            }
            val body = response.body ?: throw IOException("공유 이미지 응답이 비어 있음")
            file.outputStream().use { out -> body.byteStream().copyTo(out) }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.share.fileprovider", file)
            .toString()
    }

    /** URL 확장자. 못 알아보면 jpg — 공유 대상 앱이 MIME 을 보고 열기 때문에 크게 중요치 않다. */
    private fun String.toExtension(): String {
        val candidate = substringBefore('?').substringAfterLast('.', "")
        return candidate.takeIf { it.length in 1..4 && it.all(Char::isLetterOrDigit) } ?: "jpg"
    }

    private companion object {
        const val SHARE_DIR = "share"

        /** 콜라주 한 장 받는 데 이보다 오래 걸리면 실패로 본다. */
        const val DOWNLOAD_TIMEOUT_SECONDS = 60L
    }
}
