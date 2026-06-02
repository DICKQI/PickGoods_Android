package com.pickgoods.app.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

object ImageEditUtils {
    fun centerCropToAspectUri(
        context: Context,
        uri: Uri,
        aspectWidth: Int,
        aspectHeight: Int,
        maxDimension: Int = 2200,
        quality: Int = 92
    ): Uri {
        require(aspectWidth > 0 && aspectHeight > 0) { "裁剪比例必须大于 0" }

        val bitmap = decodeScaledBitmap(context, uri, maxDimension)
        val targetRatio = aspectWidth.toFloat() / aspectHeight.toFloat()
        val sourceRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val cropWidth: Int
        val cropHeight: Int
        if (sourceRatio > targetRatio) {
            cropHeight = bitmap.height
            cropWidth = (cropHeight * targetRatio).roundToInt().coerceIn(1, bitmap.width)
        } else {
            cropWidth = bitmap.width
            cropHeight = (cropWidth / targetRatio).roundToInt().coerceIn(1, bitmap.height)
        }

        val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
        val output = File(context.cacheDir, "pickgoods_crop_${System.currentTimeMillis()}.jpg")

        output.outputStream().use { stream ->
            cropped.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(30, 100), stream)
        }

        if (cropped !== bitmap) cropped.recycle()
        bitmap.recycle()
        return Uri.fromFile(output)
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        }

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: error("无法读取图片")
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
}
