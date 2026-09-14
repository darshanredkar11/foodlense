package com.foodlense.domain.scan

/** Platform-neutral representation of something discovered by a scan. */
data class ScanResult(
    val rawValue: String,
    val format: ScanFormat,
    val source: ScanSource
)

enum class ScanFormat { BARCODE, OCR }

enum class ScanSource { CAMERA, IMPORTED_IMAGE }
