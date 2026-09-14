package com.foodlense.android.scan

import androidx.camera.core.ImageProxy
import com.foodlense.shared.contracts.scan.ImageFormat
import com.foodlense.shared.contracts.scan.ScanFrame

/** Converts a CameraX frame into the platform-neutral scan representation. */
object CameraXFrameAdapter {
    fun toScanFrame(image: ImageProxy): ScanFrame? {
        val format = when (image.format) {
            android.graphics.ImageFormat.YUV_420_888 -> ImageFormat.YUV_420_888
            android.graphics.ImageFormat.JPEG -> ImageFormat.JPEG
            else -> return null
        }

        val planeBytes = image.planes.map { plane ->
            val buffer = plane.buffer
            ByteArray(buffer.remaining()).also(buffer::get)
        }

        return ScanFrame(
            bytes = planeBytes.fold(ByteArray(0)) { acc, bytes -> acc + bytes },
            width = image.width,
            height = image.height,
            rotationDegrees = image.imageInfo.rotationDegrees,
            format = format,
        )
    }
}
