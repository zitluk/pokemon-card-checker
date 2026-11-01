package com.pokemontcg.collection.data.scan

import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CardScanRecognizer(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(inputImage: InputImage): String =
        recognizeDetailed(inputImage).fullText

    suspend fun recognizeMergedFromBitmaps(bitmaps: List<android.graphics.Bitmap>): OcrScanResult {
        if (bitmaps.isEmpty()) {
            return OcrScanResult(fullText = "", blockCount = 0, lineCount = 0, avgConfidence = null)
        }
        val parts = mutableListOf<OcrScanResult>()
        for (bitmap in bitmaps) {
            parts.add(recognizeDetailed(InputImage.fromBitmap(bitmap, 0)))
        }
        val mergedText = parts
            .map { it.fullText.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString("\n")
        val confidences = parts.mapNotNull { it.avgConfidence }
        return OcrScanResult(
            fullText = mergedText,
            blockCount = parts.sumOf { it.blockCount },
            lineCount = parts.sumOf { it.lineCount },
            avgConfidence = confidences.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
        )
    }

    suspend fun recognizeDetailed(inputImage: InputImage): OcrScanResult =
        suspendCancellableCoroutine { cont ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val blocks = visionText.textBlocks
                    val confidences = blocks.flatMap { block ->
                        block.lines.flatMap { line ->
                            line.elements.mapNotNull { el ->
                                el.confidence.takeIf { it >= 0f }
                            }
                        }
                    }
                    val avg = confidences.takeIf { it.isNotEmpty() }?.average()?.toFloat()
                    cont.resume(
                        OcrScanResult(
                            fullText = visionText.text.orEmpty(),
                            blockCount = blocks.size,
                            lineCount = blocks.sumOf { it.lines.size },
                            avgConfidence = avg,
                        ),
                    )
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    fun close() {
        recognizer.close()
    }
}
