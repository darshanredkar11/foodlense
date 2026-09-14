package com.foodlense.android.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextScanner
import com.foodlense.shared.domain.scan.ScanCoordinator
import com.foodlense.shared.domain.scan.ScanPayload
import com.foodlense.shared.domain.scan.ScanResult
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * CameraX boundary for the shared scan pipeline.
 *
 * OCR is deliberately conservative: a single noisy frame must never become
 * the user's result. We require the same label-like text on three consecutive
 * recognition passes before delivering it. Barcode results remain immediate.
 */
class CameraAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val textScanner: TextScanner,
    private val onResult: (ScanResult) -> Unit,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val frameConverter: (ImageProxy) -> ScanFrame = { it.toScanFrame() },
) : ImageAnalysis.Analyzer, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val processing = AtomicBoolean(false)
    private val delivered = AtomicBoolean(false)
    private val coordinator = ScanCoordinator(barcodeScanner, textScanner)

    private var lastOcrText: String? = null
    private var stableOcrFrames = 0

    override fun analyze(image: ImageProxy) {
        if (delivered.get() || !processing.compareAndSet(false, true)) {
            image.close()
            return
        }

        val frame = try {
            frameConverter(image)
        } catch (_: RuntimeException) {
            null
        } finally {
            image.close()
        }

        if (frame == null) {
            processing.set(false)
            return
        }

        scope.launch {
            try {
                coordinator.scan(frame)?.let { result ->
                    if (result.payload is ScanPayload.Barcode) {
                        if (delivered.compareAndSet(false, true)) onResult(result)
                        return@let
                    }

                    val text = (result.payload as? ScanPayload.Text)?.value.orEmpty()
                    val normalized = normalizeOcrText(text)
                    if (!isLabelLike(normalized)) {
                        lastOcrText = null
                        stableOcrFrames = 0
                        return@let
                    }

                    if (normalized == lastOcrText) {
                        stableOcrFrames++
                    } else {
                        lastOcrText = normalized
                        stableOcrFrames = 1
                    }

                    if (stableOcrFrames >= REQUIRED_STABLE_OCR_FRAMES && delivered.compareAndSet(false, true)) {
                        onResult(result)
                    }
                }
            } finally {
                processing.set(false)
            }
        }
    }

    override fun close() {
        scope.cancel()
        if (barcodeScanner is AutoCloseable) barcodeScanner.close()
        if (textScanner is AutoCloseable && textScanner !== barcodeScanner) textScanner.close()
    }

    private companion object {
        const val REQUIRED_STABLE_OCR_FRAMES = 3
        val LABEL_ANCHORS = setOf(
            "ingredients", "contains", "sugar", "salt", "sodium", "oil", "flour", "wheat",
            "rice", "milk", "protein", "starch", "fat", "spice", "spices", "acid", "acidulant",
            "preservative", "emulsifier", "sweetener", "flavour", "flavor", "colour", "color",
            "e100", "e200", "e300", "e400", "e500", "e600", "e900",
        )

        fun normalizeOcrText(value: String): String = value
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()

        fun isLabelLike(text: String): Boolean {
            if (text.length < 5) return false
            if (LABEL_ANCHORS.any(text::contains)) return true
            val words = text.split(Regex("[^a-z]+" )).filter { it.length >= 2 }
            val hasListStructure = text.contains(',') || text.contains(';') || text.contains(':')
            return hasListStructure && words.size >= 2
        }
    }
}

private fun ImageProxy.toScanFrame(): ScanFrame {
    val crop = cropRect
    return ScanFrame(
        bytes = Yuv420ToNv21.convert(this),
        width = crop.width(),
        height = crop.height(),
        rotationDegrees = imageInfo.rotationDegrees,
        format = ImageFormat.YUV_420_888,
    )
}
