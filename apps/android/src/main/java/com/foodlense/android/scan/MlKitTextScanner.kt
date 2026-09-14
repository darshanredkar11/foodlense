package com.foodlense.android.scan

import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextDetection
import com.foodlense.shared.contracts.scan.TextScanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** OCR fallback adapter. Barcode remains the coordinator's preferred path. */
class MlKitTextScanner : TextScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(frame: ScanFrame): TextDetection? {
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
                    continuation.resume(result.text.takeIf { it.isNotBlank() }?.let(::TextDetection))
                }
                .addOnFailureListener { continuation.resume(null) }
        }
    }
}
