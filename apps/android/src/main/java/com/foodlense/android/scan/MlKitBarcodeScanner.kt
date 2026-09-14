package com.foodlense.android.scan

import androidx.camera.core.ImageProxy
import com.foodlense.shared.contracts.scan.BarcodeDetection
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.domain.scan.BarcodeFormat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Android ML Kit adapter. The shared coordinator remains unaware of ML Kit. */
class MlKitBarcodeScanner : BarcodeScanner {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128,
            )
            .build(),
    )

    override suspend fun scan(frame: ScanFrame): BarcodeDetection? {
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
                    val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                    continuation.resume(barcode?.let {
                        BarcodeDetection(
                            rawValue = it.rawValue!!,
                            format = it.format.toDomainFormat(),
                        )
                    })
                }
                .addOnFailureListener { continuation.resume(null) }
        }
    }

    private fun Int.toDomainFormat(): BarcodeFormat = when (this) {
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        else -> BarcodeFormat.UNKNOWN
    }
}
