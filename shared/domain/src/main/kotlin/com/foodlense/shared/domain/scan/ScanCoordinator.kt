package com.foodlense.shared.domain.scan

import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextScanner
import java.time.Clock
import java.util.UUID

/**
 * Coordinates recognition without knowing anything about Android, iOS, or web.
 * Barcode wins over OCR because it is deterministic when both are available.
 */
class ScanCoordinator(
    private val barcodeScanner: BarcodeScanner,
    private val textScanner: TextScanner,
    private val clock: Clock = Clock.systemUTC(),
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun scan(frame: ScanFrame): ScanResult? {
        barcodeScanner.scan(frame)?.let { detection ->
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
}
