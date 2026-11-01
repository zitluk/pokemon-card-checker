package com.pokemontcg.collection.data.scan

object ScanCardBackDetector {
    private val cardNumberPattern = Regex("""(\d{1,3})\s*/\s*(\d{1,3})""")
    private val hpPattern = Regex("""\b(\d{1,4})\s*hp\b""", RegexOption.IGNORE_CASE)
    private val backTrademarkWords = setOf(
        "pokemon", "pokémon", "pokÉmon", "nintendo", "creatures", "game freak", "gamefreak",
    )

    fun isLikelyCardBack(fullText: String, extraction: ScanExtraction): Boolean {
        if (extraction.cardNumberHint != null) return false
        if (extraction.tcgLocalId != null) return false
        if (hpPattern.containsMatchIn(fullText)) return false
        if (extraction.searchQueries.any { it.length >= 4 && !isTrademarkOnly(it) }) return false

        val lower = fullText.lowercase()
        val lines = fullText.lines().map { it.trim() }.filter { it.length >= 2 }
        if (lines.isEmpty()) return false

        val trademarkHits = backTrademarkWords.count { lower.contains(it) }
        val frontSignals = listOf(
            cardNumberPattern.containsMatchIn(fullText),
            hpPattern.containsMatchIn(fullText),
            extraction.searchQueries.isNotEmpty(),
            Regex("""\bex\b""", RegexOption.IGNORE_CASE).containsMatchIn(fullText) &&
                extraction.searchQueries.any { it.contains("ex", ignoreCase = true) },
        ).count { it }

        if (trademarkHits >= 2 && frontSignals == 0) return true
        if (trademarkHits >= 1 && lines.size <= 6 && frontSignals == 0) {
            val letterRatio = lower.count { it.isLetter() }.toFloat() /
                lower.length.coerceAtLeast(1)
            if (letterRatio < 0.85f) return false
            val trademarkChars = backTrademarkWords.sumOf { word ->
                if (lower.contains(word)) word.length else 0
            }
            return trademarkHits >= 1 && extraction.searchQueries.isEmpty()
        }
        return false
    }

    private fun isTrademarkOnly(line: String): Boolean {
        val lower = line.lowercase()
        return backTrademarkWords.any { lower.contains(it) } && lower.length < 24
    }
}
