package com.pokemontcg.collection.data.scan

data class OcrScanResult(
    val fullText: String,
    val blockCount: Int,
    val lineCount: Int,
    /** 0–1 when ML Kit exposes confidence; null otherwise. */
    val avgConfidence: Float?,
)
