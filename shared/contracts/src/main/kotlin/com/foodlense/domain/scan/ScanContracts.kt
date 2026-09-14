package com.foodlense.domain.scan

interface BarcodeScanner {
    suspend fun scan(image: ImageFrame): List<ScanResult>
}

interface TextScanner {
    suspend fun recognize(image: ImageFrame): List<ScanResult>
}

/** Opaque image abstraction so shared code stays independent of camera/ML frameworks. */
interface ImageFrame
