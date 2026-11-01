package com.pokemontcg.collection.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

object ScanImagePreprocessor {
    private const val MIN_OCR_WIDTH_PX = 1280

    fun inputImageForOcr(context: Context, file: File): InputImage {
        val exifRotation = readExifRotationDegrees(file)
        val decoded = decodeScaledBitmap(file) ?: return InputImage.fromFilePath(context, Uri.fromFile(file))
        val bitmap = if (exifRotation != 0) {
            rotateBitmap(decoded, exifRotation.toFloat())
        } else {
            decoded
        }
        return InputImage.fromBitmap(bitmap, 0)
    }

    private fun readExifRotationDegrees(file: File): Int = runCatching {
        when (
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrDefault(0)

    private fun decodeScaledBitmap(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null

        val targetWidth = max(bitmap.width, MIN_OCR_WIDTH_PX)
        if (bitmap.width >= targetWidth) return bitmap

        val scale = targetWidth.toFloat() / bitmap.width.toFloat()
        val newHeight = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, newHeight, true)
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var halfW = width / 2
        var halfH = height / 2
        while (halfW / sample >= MIN_OCR_WIDTH_PX * 2 && halfH / sample >= MIN_OCR_WIDTH_PX * 2) {
            sample *= 2
        }
        return sample
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
