package com.pickgoods.app.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object ImageUploadUtils {
    fun compressImageUri(
        context: Context,
        uri: Uri,
        maxSizeKb: Int = 300,
        maxDimension: Int = 1800
    ): File {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        }

        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: error("无法读取图片")

        val output = File(context.cacheDir, "pickgoods_upload_${System.currentTimeMillis()}.jpg")
        val bytes = compressBitmap(bitmap, maxSizeKb)
        output.writeBytes(bytes)
        bitmap.recycle()
        return output
    }

    private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        var scaledWidth = width
        var scaledHeight = height
        while (max(scaledWidth, scaledHeight) > maxDimension) {
            sampleSize *= 2
            scaledWidth /= 2
            scaledHeight /= 2
        }
        return sampleSize
    }

    private fun compressBitmap(bitmap: Bitmap, maxSizeKb: Int): ByteArray {
        var workingBitmap = bitmap
        var quality = 88
        var lastBytes = ByteArray(0)
        val maxBytes = maxSizeKb * 1024

        try {
            repeat(12) {
                val stream = ByteArrayOutputStream()
                workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                val bytes = stream.toByteArray()
                lastBytes = bytes
                if (bytes.size <= maxBytes) return bytes

                if (quality > 42) {
                    quality -= 8
                } else {
                    val width = (workingBitmap.width * 0.86f).toInt().coerceAtLeast(1)
                    val height = (workingBitmap.height * 0.86f).toInt().coerceAtLeast(1)
                    if (width == workingBitmap.width && height == workingBitmap.height) return bytes
                    val scaled = Bitmap.createScaledBitmap(workingBitmap, width, height, true)
                    if (workingBitmap !== bitmap) workingBitmap.recycle()
                    workingBitmap = scaled
                }
            }
        } finally {
            if (workingBitmap !== bitmap) workingBitmap.recycle()
        }

        return lastBytes
    }
}
