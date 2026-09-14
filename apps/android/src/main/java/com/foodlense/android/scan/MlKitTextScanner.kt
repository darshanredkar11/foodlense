package com.foodlense.android.scan

import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextDetection
import com.foodlense.shared.contracts.scan.TextScanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Android adapter around ML Kit Latin text recognition. */
class MlKitTextScanner : TextScanner, AutoCloseable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(frame: ScanFrame): TextDetection? {
        if (frame.format != ImageFormat.YUV_420_888 || frame.bytes.isEmpty()) return null

        val image = InputImage.fromByteArray(
            frame.bytes,
            frame.width,
            frame.height,
            frame.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21,
        )

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    val text = result.text.trim()
                    continuation.resume(text.takeIf { it.isNotEmpty() }?.let(::TextDetection))
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }

    override fun close() {
        recognizer.close()
    }
}
