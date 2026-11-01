package com.pokemontcg.collection.data.scan

data class ScanExtraction(
    val searchQueries: List<String>,
    val cardNumberHint: String?,
    val rawPreview: String,
    val fallbackTokens: List<String> = emptyList(),
)

object ScanQueryExtractor {
    private val cardNumberPattern = Regex("""(\d{1,4})\s*/\s*(\d{1,4})""")
    private val noiseWords = setOf(
        "basic", "stage", "pokémon", "pokemon", "trainer", "energy", "hp", "weakness",
        "resistance", "retreat", "illus", "rarity", "promo", "gx", "ex", "vmax", "vstar",
        "ability", "attack", "coin", "flip", "damage", "put", "your", "the", "and",
    )

    fun extract(fullText: String): ScanExtraction {
        val lines = fullText
            .lines()
            .map { cleanLine(it) }
            .filter { it.length >= 2 }

        var cardNumber: String? = null
        val queries = linkedSetOf<String>()

        for (line in lines) {
            cardNumberPattern.find(line)?.let { match ->
                cardNumber = "${match.groupValues[1]}/${match.groupValues[2]}"
            }
            if (line.contains("hp", ignoreCase = true) && line.any { it.isDigit() }) continue
            if (isLikelyName(line)) {
                queries.add(line)
                queries.add(line.substringBefore(" ").trim())
            }
        }

        val blob = lines.joinToString(" ")
        cardNumberPattern.find(blob)?.let { match ->
            cardNumber = cardNumber ?: "${match.groupValues[1]}/${match.groupValues[2]}"
        }

        extractCapitalizedRuns(blob).forEach { queries.add(it) }
        extractAllCapsNames(blob).forEach { queries.add(it) }

        for (line in lines) {
            if (line.any { it.isLetter() }) {
                queries.add(normalizeNameForSearch(line))
            }
        }

        val fallbackTokens = extractFallbackTokens(blob)

        val ranked = queries
            .map { it.trim() }
            .filter { it.length in 2..40 }
            .distinctBy { it.lowercase() }
            .sortedByDescending { it.length }
            .take(10)

        return ScanExtraction(
            searchQueries = ranked,
            cardNumberHint = cardNumber,
            rawPreview = lines.take(8).joinToString(" · "),
            fallbackTokens = fallbackTokens,
        )
    }

    private fun normalizeNameForSearch(line: String): String =
        line.trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { ch -> ch.titlecase() }
            }

    private fun extractAllCapsNames(text: String): List<String> {
        val pattern = Regex("""\b[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ0-9'\-]{2,}(?:\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ0-9'\-]{2,}){0,3}\b""")
        return pattern.findAll(text)
            .map { normalizeNameForSearch(it.value) }
            .filter { isLikelyName(it) }
            .toList()
    }

    private fun extractFallbackTokens(blob: String): List<String> =
        blob.split(Regex("""\s+"""))
            .map { it.trim().trim('"', '\'', '.', ',') }
            .filter { token ->
                token.length in 4..24 &&
                    token.any { it.isLetter() } &&
                    !token.equals("pokemon", ignoreCase = true) &&
                    !cardNumberPattern.matches(token)
            }
            .map { normalizeNameForSearch(it) }
            .distinct()
            .take(8)

    private fun cleanLine(raw: String): String =
        raw.trim()
            .replace(Regex("""[^\p{L}\p{N}\s/'-]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun isLikelyName(line: String): Boolean {
        if (line.length !in 3..36) return false
        val lower = line.lowercase()
        if (noiseWords.any { lower == it || lower.startsWith("$it ") }) return false
        if (line.count { it.isLetter() } < 3) return false
        if (cardNumberPattern.matches(line)) return false
        if (line.all { it.isDigit() || it == '/' || it.isWhitespace() }) return false
        return true
    }

    private fun extractCapitalizedRuns(text: String): List<String> {
        val runs = Regex("""([A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][a-záčďéěíňóřšťúůýž]+(?:\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][a-záčďéěíňóřšťúůýž]+){0,3})""")
            .findAll(text)
            .map { it.value.trim() }
            .filter { isLikelyName(it) }
            .toList()
        return runs
    }
}
