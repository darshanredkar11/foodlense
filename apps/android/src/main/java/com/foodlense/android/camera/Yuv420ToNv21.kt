package com.foodlense.android.camera

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/** Converts a CameraX YUV_420_888 crop into the NV21 layout expected by ML Kit. */
internal object Yuv420ToNv21 {
    fun convert(image: ImageProxy): ByteArray {
        require(image.format == android.graphics.ImageFormat.YUV_420_888) {
            "Expected YUV_420_888, got ${image.format}"
        }

        val crop = image.cropRect
        val width = crop.width()
        val height = crop.height()
        require(width > 0 && height > 0 && width % 2 == 0 && height % 2 == 0) {
            "YUV crop must have positive even dimensions: ${width}x$height"
        }
        require(crop.left >= 0 && crop.top >= 0 && crop.right <= image.width && crop.bottom <= image.height) {
            "YUV crop is outside the image bounds: $crop for ${image.width}x${image.height}"
        }
        require(image.planes.size >= 3) { "YUV_420_888 must contain three planes" }

        val output = ByteArray(width * height * 3 / 2)
        var outputOffset = copyPlane(
            plane = image.planes[0],
            startX = crop.left,
            startY = crop.top,
            width = width,
            height = height,
            output = output,
            outputOffset = 0,
        )

        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val uBuffer = uPlane.buffer.duplicate()
        val vBuffer = vPlane.buffer.duplicate()
        val chromaWidth = width / 2
        val chromaHeight = height / 2
        val chromaStartX = crop.left / 2
        val chromaStartY = crop.top / 2

        for (row in 0 until chromaHeight) {
            val uRowStart = (chromaStartY + row) * uPlane.rowStride
            val vRowStart = (chromaStartY + row) * vPlane.rowStride
            for (column in 0 until chromaWidth) {
                val chromaColumn = chromaStartX + column
                val uIndex = uRowStart + chromaColumn * uPlane.pixelStride
                val vIndex = vRowStart + chromaColumn * vPlane.pixelStride
                require(uIndex in 0 until uBuffer.limit() && vIndex in 0 until vBuffer.limit()) {
                    "YUV chroma plane does not contain the requested crop"
                }
                output[outputOffset++] = vBuffer.get(vIndex)
                output[outputOffset++] = uBuffer.get(uIndex)
            }
        }

        return output
    }

    private fun copyPlane(
        plane: ImageProxy.PlaneProxy,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        output: ByteArray,
        outputOffset: Int,
    ): Int {
        val buffer: ByteBuffer = plane.buffer.duplicate()
        var offset = outputOffset
        for (row in 0 until height) {
            val rowStart = (startY + row) * plane.rowStride
            for (column in 0 until width) {
                val index = rowStart + (startX + column) * plane.pixelStride
                require(index in 0 until buffer.limit()) {
                    "YUV luma plane does not contain the requested crop"
                }
                output[offset++] = buffer.get(index)
            }
        }
        return offset
    }
}
