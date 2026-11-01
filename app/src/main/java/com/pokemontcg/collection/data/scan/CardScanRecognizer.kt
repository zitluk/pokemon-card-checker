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
