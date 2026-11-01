package com.pokemontcg.collection.data.scan

data class ScanExtraction(
    val searchQueries: List<String>,
    val cardNumberHint: String?,
    val tcgLocalId: String?,
    val setTotal: Int?,
    val setHints: List<String>,
    val rawPreview: String,
    val fallbackTokens: List<String> = emptyList(),
)

object ScanQueryExtractor {
    private val cardNumberPattern = Regex("""(\d{1,3})\s*/\s*(\d{1,3})""")
    private val hpLinePattern = Regex("""\b(\d{1,4})\s*hp\b""", RegexOption.IGNORE_CASE)
    private val inventorySkPattern = Regex("""^SK\s*:\s*K\d+""", RegexOption.IGNORE_CASE)
    private val dateLinePattern = Regex("""^\d{2}\.\d{2}\.\d{2}(\.\d+)?$""")
    private val pozicePattern = Regex("""pozice\s*:""", RegexOption.IGNORE_CASE)
    private val nameWithExPattern = Regex(
        """\b([A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽa-záčďéěíňóřšťúůýž]+)\s+ex\b""",
        RegexOption.IGNORE_CASE,
    )
    private val noiseWords = setOf(
        "basic", "stage", "pokémon", "pokemon", "trainer", "energy", "weakness",
        "resistance", "retreat", "illus", "rarity", "promo", "gx", "vmax", "vstar",
        "ability", "attack", "coin", "flip", "damage", "put", "your", "the", "and",
    )

    fun extract(fullText: String): ScanExtraction {
        val rawLines = fullText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val lines = rawLines
            .filterNot { isNoiseLine(it) }
            .map { cleanLine(it) }
            .filter { it.length >= 2 }

        var cardNumber: String? = null
        var tcgLocalId: String? = null
        var setTotal: Int? = null
        val queries = linkedSetOf<String>()

        for (line in lines) {
            cardNumberPattern.find(line)?.let { match ->
                val num = match.groupValues[1]
                val total = match.groupValues[2].toIntOrNull()
                cardNumber = "${num.padStart(3, '0')}/${total ?: match.groupValues[2]}"
                tcgLocalId = normalizeTcgLocalId(num, total)
                setTotal = total
            }
            if (hpLinePattern.containsMatchIn(line)) continue
            if (isLikelyName(line)) {
                queries.add(line)
                queries.add(line.substringBefore(" ").trim())
            }
        }

        val blob = lines.joinToString(" ")
        cardNumberPattern.find(blob)?.let { match ->
            if (cardNumber == null) {
                val num = match.groupValues[1]
                val total = match.groupValues[2].toIntOrNull()
                cardNumber = "${num.padStart(3, '0')}/${total ?: match.groupValues[2]}"
                tcgLocalId = normalizeTcgLocalId(num, total)
                setTotal = total
            }
        }

        nameWithExPattern.findAll(blob).forEach { match ->
            val name = normalizeNameForSearch(match.groupValues[1]) + " ex"
            queries.add(name)
            queries.add(name.substringBefore(" ex").trim())
        }

        extractCapitalizedRuns(blob).forEach { queries.add(it) }
        extractAllCapsNames(blob).forEach { queries.add(it) }

        for (line in lines) {
            if (line.any { it.isLetter() }) {
                queries.add(normalizeNameForSearch(line))
            }
        }

        val setHints = SetHintResolver.extractSetIds(fullText).toMutableList()
        if (setTotal == 128 && !setHints.contains("30th")) {
            setHints.add("30th")
        }
        tcgLocalId?.let { id ->
            setHints.forEach { setId ->
                queries.add(SetHintResolver.tcgDexCardId(setId, id))
            }
        }

        val fallbackTokens = extractFallbackTokens(blob)

        val ranked = queries
            .map { it.trim() }
            .filter { it.length in 2..40 }
            .filterNot { it.contains("-") && it.count { c -> c.isDigit() } > 6 }
            .distinctBy { it.lowercase() }
            .sortedWith(
                compareByDescending<String> { tcgLocalId != null && it == tcgLocalId }
                    .thenByDescending { it.contains(" ex", ignoreCase = true) }
                    .thenByDescending { it.length },
            )
            .take(12)

        return ScanExtraction(
            searchQueries = ranked,
            cardNumberHint = cardNumber,
            tcgLocalId = tcgLocalId,
            setTotal = setTotal,
            setHints = setHints,
            rawPreview = lines.take(8).joinToString(" · "),
            fallbackTokens = fallbackTokens,
        )
    }

    fun normalizeTcgLocalId(numerator: String, setTotal: Int?): String {
        val digits = numerator.trim()
        return if (setTotal == 128 || digits.length <= 3) {
            digits.padStart(3, '0')
        } else {
            digits
        }
    }

    private fun isNoiseLine(raw: String): Boolean {
        val t = raw.trim()
        if (t.length < 2) return true
        if (inventorySkPattern.containsMatchIn(t)) return true
        if (dateLinePattern.matches(t.replace(" ", ""))) return true
        if (pozicePattern.containsMatchIn(t)) return true
        if (t.all { it.isDigit() || it == ' ' }) return true
        val digits = t.count { it.isDigit() }
        if (digits > t.length * 0.85 && t.length > 8) return true
        return false
    }

    private fun normalizeNameForSearch(line: String): String =
        line.trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                when {
                    word.equals("ex", ignoreCase = true) -> "ex"
                    else -> word.lowercase().replaceFirstChar { ch -> ch.titlecase() }
                }
            }

    private fun extractAllCapsNames(text: String): List<String> {
        val pattern = Regex("""\b[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ0-9'\-]{2,}(?:\s+(?:[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ0-9'\-]{2,}|ex)){0,3}\b""")
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
        if (cardNumberPattern.matches(line.replace(" ", ""))) return false
        if (line.all { it.isDigit() || it == '/' || it.isWhitespace() }) return false
        return true
    }

    private fun extractCapitalizedRuns(text: String): List<String> {
        val runs = Regex("""([A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][a-záčďéěíňóřšťúůýž]+(?:\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][a-záčďéěíňóřšťúůýž]+){0,2}(?:\s+ex)?)""")
            .findAll(text)
            .map { it.value.trim() }
            .filter { isLikelyName(it.replace(" ex", "", ignoreCase = true)) }
            .toList()
        return runs
    }
}
