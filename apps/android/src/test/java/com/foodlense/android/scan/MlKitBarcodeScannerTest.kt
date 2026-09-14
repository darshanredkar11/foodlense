package com.foodlense.android.scan

import com.foodlense.shared.domain.scan.BarcodeFormat
import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.test.Test
import kotlin.test.assertEquals

class MlKitBarcodeScannerTest {
    @Test
    fun `supported ML Kit formats map to domain formats`() {
        assertEquals(BarcodeFormat.EAN_8, Barcode.FORMAT_EAN_8.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.EAN_13, Barcode.FORMAT_EAN_13.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.UPC_A, Barcode.FORMAT_UPC_A.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.UPC_E, Barcode.FORMAT_UPC_E.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.CODE_128, Barcode.FORMAT_CODE_128.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.QR_CODE, Barcode.FORMAT_QR_CODE.toDomainBarcodeFormat())
        assertEquals(BarcodeFormat.DATA_MATRIX, Barcode.FORMAT_DATA_MATRIX.toDomainBarcodeFormat())
    }

    @Test
    fun `unknown ML Kit format maps safely`() {
        assertEquals((-12345).toDomainBarcodeFormat(), BarcodeFormat.UNKNOWN)
    }
}
