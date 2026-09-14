package com.foodlense.android.scan

import com.foodlense.shared.contracts.scan.BarcodeDetection
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.domain.scan.BarcodeFormat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Android adapter around ML Kit barcode recognition. */
class MlKitBarcodeScanner : BarcodeScanner, AutoCloseable {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX,
            )
            .build(),
    )

    override suspend fun scan(frame: ScanFrame): BarcodeDetection? {
        if (frame.format != ImageFormat.YUV_420_888 || frame.bytes.isEmpty()) return null

        val image = InputImage.fromByteArray(
            frame.bytes,
            frame.width,
            frame.height,
            frame.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21,
        )

        return suspendCancellableCoroutine { continuation ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(
                        barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.let { barcode ->
                            BarcodeDetection(
                                rawValue = barcode.rawValue.orEmpty(),
                                format = barcode.format.toDomainBarcodeFormat(),
                            )
                        },
                    )
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }

    override fun close() {
        scanner.close()
    }
}

internal fun Int.toDomainBarcodeFormat(): BarcodeFormat = when (this) {
    Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
    Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
    Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
    Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
    Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
    Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
    Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
    else -> BarcodeFormat.UNKNOWN
}
