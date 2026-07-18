package com.makeus.mody.feature.record.camera

import android.content.Context
import android.util.Rational
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import java.io.File
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** CameraX Preview + ImageCapture 를 [lifecycleOwner] 에 바인딩. 성공 시 ImageCapture 반환. */
suspend fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    lensFacing: Int,
): ImageCapture = suspendCoroutine { cont ->
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        val provider = future.get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            // 센서 최고 해상도로 캡처(기본값이 낮게 잡히는 것 방지).
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                    .build(),
            )
            .build()
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview 와 ImageCapture 가 같은 크롭 영역(화면 비율) 공유 → 보이는 대로 찍힘(WYSIWYG).
        val metrics = context.resources.displayMetrics
        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val viewPort = ViewPort.Builder(
            Rational(metrics.widthPixels, metrics.heightPixels),
            rotation,
        ).build()
        val useCaseGroup = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(preview)
            .addUseCase(imageCapture)
            .build()

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selector, useCaseGroup)
        cont.resume(imageCapture)
    }, ContextCompat.getMainExecutor(context))
}

/** 사진 촬영 → [file] 저장. 성공 시 파일 절대경로 반환, 실패 시 예외. */
suspend fun ImageCapture.capture(context: Context, file: File): String =
    suspendCoroutine { cont ->
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cont.resume(file.absolutePath)
                }

                override fun onError(exc: ImageCaptureException) {
                    cont.resumeWith(Result.failure(exc))
                }
            },
        )
    }

