package com.foodlense.shared.domain.scan

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
        assertEquals(BarcodeFormat.EAN_13, (result.payload as ScanPayload.Barcode).format)
        assertEquals(0f, result.confidence ?: 0f)
        assertEquals("scan-1", result.id)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), result.capturedAt)
    }

    @Test
    fun `barcode confidence is propagated`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { BarcodeDetection("12345678", BarcodeFormat.EAN_8, 0.92f) },
            textScanner = TextScanner { TextDetection("ignored") },
            clock = clock,
        )

        assertEquals(0.92f, coordinator.scan(frame)?.confidence)
    }

    @Test
    fun `OCR is used when barcode is absent`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { null },
            textScanner = TextScanner { TextDetection("Nutrition Facts", 0.81f) },
            clock = clock,
            idFactory = { "scan-2" },
        )

        val result = coordinator.scan(frame)

        assertEquals(ScanSource.OCR, result?.source)
        assertEquals("Nutrition Facts", (result?.payload as ScanPayload.Text).value)
        assertEquals(0.81f, result.confidence)
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

    @Test
    fun `OCR whitespace around text is retained as returned by scanner`() = runTest {
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { null },
            textScanner = TextScanner { TextDetection("  Milk  ") },
            clock = clock,
        )

        assertEquals("  Milk  ", (coordinator.scan(frame)?.payload as ScanPayload.Text).value)
    }

    @Test
    fun `barcode scanner is not followed by OCR when barcode succeeds`() = runTest {
        var ocrCalls = 0
        val coordinator = ScanCoordinator(
            barcodeScanner = BarcodeScanner { BarcodeDetection("8901234567890", BarcodeFormat.EAN_13) },
            textScanner = TextScanner {
                ocrCalls++
                TextDetection("must not run")
            },
        )

        coordinator.scan(frame)

        assertEquals(0, ocrCalls)
    }
}
