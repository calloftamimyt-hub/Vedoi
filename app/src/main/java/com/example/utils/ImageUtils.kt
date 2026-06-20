package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageUtils {
    fun cropAndSaveImage(context: Context, sourceUri: Uri, zoomScale: Float): String {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap != null) {
                val width = originalBitmap.width
                val height = originalBitmap.height
                val size = min(width, height)
                
                // Base square coordinates
                val x = (width - size) / 2
                val y = (height - size) / 2
                
                // Apply the zoom percentage crop
                val croppedSize = (size / zoomScale).toInt().coerceAtLeast(100).coerceAtMost(size)
                val newX = (x + (size - croppedSize) / 2).coerceIn(0, width - croppedSize)
                val newY = (y + (size - croppedSize) / 2).coerceIn(0, height - croppedSize)
                
                val croppedBitmap = Bitmap.createBitmap(originalBitmap, newX, newY, croppedSize, croppedSize)
                
                // Save to internal filesystem (sandboxed, persistent, doesn't need permissions once in app context files)
                val directory = File(context.filesDir, "avatars")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, "avatar_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                croppedBitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
                outputStream.flush()
                outputStream.close()
                
                return file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sourceUri.toString()
    }
}
