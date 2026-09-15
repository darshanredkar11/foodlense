package com.foodlense.shared.domain.scan

import java.time.Clock
import java.util.UUID

/**
 * Coordinates recognition without knowing anything about Android, iOS, or web.
 *
 * Retail product barcodes (EAN/UPC) are useful product identifiers and win over OCR.
 * QR/Data Matrix codes are deliberately not treated as ingredient data: packaging
 * often contains URLs, addresses, or marketing codes that can create a false
 * "healthy" result when interpreted as label text.
 */
class ScanCoordinator(
    private val barcodeScanner: BarcodeScanner,
    private val textScanner: TextScanner,
    private val clock: Clock = Clock.systemUTC(),
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun scan(frame: ScanFrame): ScanResult? {
        barcodeScanner.scan(frame)
            ?.takeIf { it.format.isRetailProductBarcode() }
            ?.let { detection ->
                return ScanResult(
                    id = idFactory(),
                    capturedAt = clock.instant(),
                    source = ScanSource.BARCODE,
                    payload = detection.asPayload(),
                    confidence = detection.confidence,
                )
            }

        textScanner.recognize(frame)?.takeIf { it.text.isNotBlank() }?.let { detection ->
            return ScanResult(
                id = idFactory(),
                capturedAt = clock.instant(),
                source = ScanSource.OCR,
                payload = detection.asPayload(),
                confidence = detection.confidence,
            )
        }

        return null
    }

    private fun BarcodeFormat.isRetailProductBarcode(): Boolean = when (this) {
        BarcodeFormat.EAN_8,
        BarcodeFormat.EAN_13,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        -> true
        BarcodeFormat.CODE_128,
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.UNKNOWN,
        -> false
    }
}
