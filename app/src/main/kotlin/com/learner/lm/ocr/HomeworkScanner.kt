package com.learner.lm.ocr

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class HomeworkScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractText(bitmap: Bitmap): Result<String> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text.trim()
                if (text.isBlank()) {
                    continuation.resume(Result.failure(IllegalStateException("No text detected in image")))
                } else {
                    continuation.resume(Result.success(text))
                }
            }
            .addOnFailureListener { error ->
                continuation.resume(Result.failure(error))
            }
    }

    suspend fun extractText(imageProxy: ImageProxy): Result<String> {
        val mediaImage = imageProxy.image
            ?: return Result.failure(IllegalStateException("Camera image unavailable"))
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.trim()
                    if (text.isBlank()) {
                        continuation.resume(Result.failure(IllegalStateException("No text detected in image")))
                    } else {
                        continuation.resume(Result.success(text))
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    fun close() {
        recognizer.close()
    }
}
