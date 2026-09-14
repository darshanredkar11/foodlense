package com.foodlense.android.camera

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/** Converts CameraX YUV_420_888 frames into the NV21 layout expected by ML Kit. */
internal object Yuv420ToNv21 {
    fun convert(image: ImageProxy): ByteArray {
        require(image.format == android.graphics.ImageFormat.YUV_420_888) {
            "Expected YUV_420_888, got ${image.format}"
        }

        val width = image.width
        val height = image.height
        val output = ByteArray(width * height * 3 / 2)
        var outputOffset = 0

        outputOffset = copyPlane(
            plane = image.planes[0],
            width = width,
            height = height,
            output = output,
            outputOffset = outputOffset,
        )

        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val uBuffer = uPlane.buffer.duplicate()
        val vBuffer = vPlane.buffer.duplicate()
        val chromaWidth = width / 2
        val chromaHeight = height / 2

        for (row in 0 until chromaHeight) {
            val uRowStart = row * uPlane.rowStride
            val vRowStart = row * vPlane.rowStride
            for (column in 0 until chromaWidth) {
                val uIndex = uRowStart + column * uPlane.pixelStride
                val vIndex = vRowStart + column * vPlane.pixelStride
                output[outputOffset++] = vBuffer.get(vIndex)
                output[outputOffset++] = uBuffer.get(uIndex)
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
    ): Int {
        val buffer: ByteBuffer = plane.buffer.duplicate()
        var offset = outputOffset
        for (row in 0 until height) {
            val rowStart = row * plane.rowStride
            for (column in 0 until width) {
                output[offset++] = buffer.get(rowStart + column * plane.pixelStride)
            }
        }
        return offset
    }
}
