package com.pokemontcg.collection.data.scan

/**
 * Heuristic authenticity hint only — not grading or legal verification.
 */
object AuthenticityEstimator {
    fun estimate(
        ocr: OcrScanResult,
        matchScore: Int,
        aspectRatioScore: Int,
    ): Int {
        val textLen = ocr.fullText.trim().length
        val clarity = when {
            textLen >= 120 -> 90
            textLen >= 60 -> 75
            textLen >= 30 -> 55
            textLen >= 12 -> 35
            else -> 15
        }
        val blockBonus = when {
            ocr.blockCount >= 6 -> 12
            ocr.blockCount >= 3 -> 8
            ocr.blockCount >= 1 -> 4
            else -> 0
        }
        val confidenceBonus = ocr.avgConfidence?.let { (it * 20).toInt().coerceIn(0, 20) } ?: 0
        val ocrComponent = (clarity + blockBonus + confidenceBonus).coerceAtMost(100)

        val weighted = (
            ocrComponent * 0.35 +
                matchScore.coerceIn(0, 100) * 0.45 +
                aspectRatioScore.coerceIn(0, 100) * 0.20
            ).toInt()
        return weighted.coerceIn(0, 100)
    }

    /** Pokémon TCG card photos are usually taller than wide (~2.5:3.5). */
    fun aspectRatioScore(width: Int, height: Int): Int {
        if (width <= 0 || height <= 0) return 40
        val ratio = width.toFloat() / height.toFloat()
        return when {
            ratio in 0.62f..0.82f -> 95
            ratio in 0.55f..0.90f -> 75
            ratio in 0.45f..1.05f -> 50
            else -> 25
        }
    }
}
