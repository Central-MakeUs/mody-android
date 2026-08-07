package com.makeus.mody.feature.challenge.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * 인증 사진 촬영 결과를 받을 캐시 파일의 공유 URI.
 * 경로는 `challenge_file_paths.xml` 의 cache-path(challenge/camera)와 맞춰야 한다.
 */
internal fun createProofCaptureUri(context: Context): Uri {
    val dir = File(context.cacheDir, "challenge/camera").apply { mkdirs() }
    val file = File(dir, "proof_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.challenge.fileprovider",
        file,
    )
}
