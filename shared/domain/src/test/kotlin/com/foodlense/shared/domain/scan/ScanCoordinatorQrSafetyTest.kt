package com.foodlense.shared.domain.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class ScanCoordinatorQrSafetyTest {
    private val frame = ScanFrame(ByteArray(0), 100, 100, 0, ImageFormat.JPEG)

    @Test
    fun `QR code cannot be interpreted as ingredient label`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = object : BarcodeScanner {
                override suspend fun scan(frame: ScanFrame) =
                    BarcodeDetection("https://example.com/product", BarcodeFormat.QR_CODE)
            },
            textScanner = object : TextScanner {
                override suspend fun recognize(frame: ScanFrame) =
                    TextDetection("Ingredients: carbonated water, sugar, acidity regulator")
            },
        )

        val result = checkNotNull(coordinator.scan(frame))

        assertEquals(ScanSource.OCR, result.source)
        assertEquals(
            "Ingredients: carbonated water, sugar, acidity regulator",
            (result.payload as ScanPayload.Text).value,
        )
    }
}
