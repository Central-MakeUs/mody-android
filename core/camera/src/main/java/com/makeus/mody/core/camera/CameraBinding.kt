package com.makeus.mody.core.camera

import android.content.Context
import android.util.Rational
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import java.io.File
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** 촬영 결과 비율(가로:세로). 프리뷰 박스도 이 비율로 그린다. */
internal const val CAPTURE_ASPECT_WIDTH = 3
internal const val CAPTURE_ASPECT_HEIGHT = 4

/** CameraX Preview + ImageCapture 를 [lifecycleOwner] 에 바인딩. 성공 시 ImageCapture 반환. */
internal suspend fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    lensFacing: Int,
): ImageCapture = suspendCoroutine { cont ->
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        val provider = future.get()
        // 둘 다 센서 기본 비율(4:3) 스트림을 고른다. 16:9 를 받아 3:4 로 자르면 화각이 좁아진다.
        // 프리뷰는 해상도 전략을 주지 않는다 — CameraX 기본값(화면 크기 또는 1080p 중 작은 쪽)이면
        // 충분하고, 최고 해상도로 잡으면 고화소 기기에서 메모리·전력만 더 쓴다.
        val previewSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
        // 촬영은 센서 최고 해상도로(기본값이 낮게 잡히는 것 방지).
        val captureSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .build()
        val preview = Preview.Builder()
            .setResolutionSelector(previewSelector)
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(captureSelector)
            .build()
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview 와 ImageCapture 가 같은 크롭 영역(3:4 세로)을 공유 → 보이는 대로 찍힘(WYSIWYG).
        // 화면 비율로 잡으면 기기마다 결과 비율이 달라진다(iOS 와 맞춘 3:4 로 고정).
        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val viewPort = ViewPort.Builder(
            Rational(CAPTURE_ASPECT_WIDTH, CAPTURE_ASPECT_HEIGHT),
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
internal suspend fun ImageCapture.capture(context: Context, file: File): String =
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

