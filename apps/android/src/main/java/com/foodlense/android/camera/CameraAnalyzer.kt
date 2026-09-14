package com.foodlense.android.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextScanner
import com.foodlense.shared.domain.scan.ScanCoordinator
import com.foodlense.shared.domain.scan.ScanResult
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
 * The ImageProxy is converted and closed immediately. ML Kit receives an
 * immutable NV21 frame, so the camera buffer is never held across suspension.
 * At most one recognition operation is active at a time, and one analyzer
 * instance delivers at most one successful result.
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
                    if (delivered.compareAndSet(false, true)) {
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
