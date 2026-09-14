package com.foodlense.android.scan

import androidx.camera.core.ImageProxy

/** Converts the common CameraX YUV_420_888 layout into NV21 for ML Kit. */
object Yuv420ToNv21 {
    fun convert(image: ImageProxy): ByteArray {
        require(image.format == android.graphics.ImageFormat.YUV_420_888)

        val width = image.width
        val height = image.height
        val ySize = width * height
        val output = ByteArray(ySize + (ySize / 2))

        copyPlane(image.planes[0], width, height, output, 0)

        val u = image.planes[1]
        val v = image.planes[2]
        var offset = ySize
        for (row in 0 until height / 2) {
            var col = 0
            while (col < width / 2) {
                val uIndex = row * u.rowStride + col * u.pixelStride
                val vIndex = row * v.rowStride + col * v.pixelStride
                output[offset++] = v.buffer.get(vIndex)
                output[offset++] = u.buffer.get(uIndex)
                col++
            }
        }
        return output
    }

    private fun copyPlane(
        plane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int,
        output: ByteArray,
        outputOffset: Int,
    ) {
        var offset = outputOffset
        for (row in 0 until height) {
            val rowStart = row * plane.rowStride
            for (col in 0 until width) {
                output[offset++] = plane.buffer.get(rowStart + col * plane.pixelStride)
            }
        }
    }
}
