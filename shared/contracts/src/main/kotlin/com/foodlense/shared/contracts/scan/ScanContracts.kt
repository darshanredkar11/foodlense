package com.foodlense.shared.contracts.scan

/**
 * Public cross-client scan capability contracts.
 *
 * The domain module owns the actual scan primitives; this package exposes them
 * to platform clients without creating a dependency cycle back into contracts.
 */
typealias BarcodeScanner = com.foodlense.shared.domain.scan.BarcodeScanner
typealias TextScanner = com.foodlense.shared.domain.scan.TextScanner
typealias ScanFrame = com.foodlense.shared.domain.scan.ScanFrame
typealias ImageFormat = com.foodlense.shared.domain.scan.ImageFormat
typealias BarcodeDetection = com.foodlense.shared.domain.scan.BarcodeDetection
typealias TextDetection = com.foodlense.shared.domain.scan.TextDetection
