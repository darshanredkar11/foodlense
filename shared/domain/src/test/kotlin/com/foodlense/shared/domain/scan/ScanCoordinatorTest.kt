package com.foodlense.shared.domain.scan

import com.foodlense.shared.contracts.scan.BarcodeDetection
import com.foodlense.shared.contracts.scan.BarcodeScanner
import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame
import com.foodlense.shared.contracts.scan.TextDetection
import com.foodlense.shared.contracts.scan.TextScanner
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class ScanCoordinatorTest {
    private val frame = ScanFrame(ByteArray(0), 100, 100, 0, ImageFormat.JPEG)
    private val clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `barcode result wins over OCR`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { BarcodeDetection("8901234567890", BarcodeFormat.EAN_13) },
            textScanner = TextScanner { TextDetection("ignored") },
            clock = clock,
            idFactory = { "scan-1" },
        )

        val result = coordinator.scan(frame)

        assertEquals(ScanSource.BARCODE, result?.source)
        assertEquals("8901234567890", (result?.payload as ScanPayload.Barcode).rawValue)
        assertEquals("scan-1", result?.id)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), result?.capturedAt)
    }

    @Test
    fun `OCR is used when barcode is absent`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { null },
            textScanner = TextScanner { TextDetection("Nutrition Facts") },
            clock = clock,
            idFactory = { "scan-2" },
        )

        val result = coordinator.scan(frame)

        assertEquals(ScanSource.OCR, result?.source)
        assertEquals("Nutrition Facts", (result?.payload as ScanPayload.Text).value)
    }

    @Test
    fun `blank OCR is ignored`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { null },
            textScanner = TextScanner { TextDetection("   ") },
            clock = clock,
        )

        assertNull(coordinator.scan(frame))
    }
}
