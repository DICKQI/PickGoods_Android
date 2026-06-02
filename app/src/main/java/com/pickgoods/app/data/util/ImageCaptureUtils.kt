package com.pickgoods.app.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageCaptureUtils {
    fun createCaptureUri(context: Context): Uri {
        val directory = File(context.cacheDir, "camera").apply {
            mkdirs()
        }
        val file = File(directory, "pickgoods_camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
