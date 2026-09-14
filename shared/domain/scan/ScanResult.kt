package com.foodlense.shared.domain.scan

import java.time.Instant

/** A platform-neutral result produced by a scan attempt. */
data class ScanResult(
    val id: String,
    val capturedAt: Instant,
    val source: ScanSource,
    val payload: ScanPayload,
    val confidence: Float? = null,
)

enum class ScanSource {
    BARCODE,
    OCR,
    IMAGE,
}

sealed interface ScanPayload {
    data class Barcode(
        val rawValue: String,
        val format: BarcodeFormat,
    ) : ScanPayload

    data class Text(
        val value: String,
    ) : ScanPayload
}

enum class BarcodeFormat {
    EAN_8,
    EAN_13,
    UPC_A,
    UPC_E,
    CODE_128,
    QR_CODE,
    DATA_MATRIX,
    UNKNOWN,
}
