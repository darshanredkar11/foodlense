package com.foodlense.shared.domain.scan

/** Scanner capabilities implemented by each platform. */
interface BarcodeScanner {
    suspend fun scan(frame: ScanFrame): BarcodeDetection?
}

interface TextScanner {
    suspend fun recognize(frame: ScanFrame): TextDetection?
}

data class ScanFrame(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val format: ImageFormat,
)

enum class ImageFormat {
    JPEG,
    YUV_420_888,
    RGBA_8888,
}

data class BarcodeDetection(
    val rawValue: String,
    val format: BarcodeFormat,
    val confidence: Float? = null,
) {
    fun asPayload(): ScanPayload.Barcode = ScanPayload.Barcode(rawValue, format)
}

data class TextDetection(
    val text: String,
    val confidence: Float? = null,
) {
    fun asPayload(): ScanPayload.Text = ScanPayload.Text(text)
}
