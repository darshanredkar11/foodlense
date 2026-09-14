package com.foodlense.android.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextScanner
import com.foodlense.shared.domain.scan.ScanCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * CameraX boundary: one frame is processed at a time and always closed.
 * KEEP_ONLY_LATEST prevents an overloaded analyzer from building a frame queue.
 */
class CameraAnalyzer(
    barcodeScanner: BarcodeScanner,
    textScanner: TextScanner,
    private val onResult: (com.foodlense.shared.domain.scan.ScanResult) -> Unit,
) : ImageAnalysis.Analyzer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val coordinator = ScanCoordinator(barcodeScanner, textScanner)

    override fun analyze(image: ImageProxy) {
        val frame = image.toScanFrame()
        scope.launch {
            try {
                coordinator.scan(frame)?.let(onResult)
            } finally {
                image.close()
            }
        }
    }
}

private fun ImageProxy.toScanFrame(): ScanFrame = ScanFrame(
    bytes = planes.firstOrNull()?.buffer?.let { buffer ->
        ByteArray(buffer.remaining()).also(buffer::get)
    } ?: ByteArray(0),
    width = width,
    height = height,
    rotationDegrees = imageInfo.rotationDegrees,
    format = when (format) {
        android.graphics.ImageFormat.YUV_420_888 -> com.foodlense.shared.contracts.scan.ImageFormat.YUV_420_888
        android.graphics.ImageFormat.JPEG -> com.foodlense.shared.contracts.scan.ImageFormat.JPEG
        else -> com.foodlense.shared.contracts.scan.ImageFormat.RGBA_8888
    },
)
