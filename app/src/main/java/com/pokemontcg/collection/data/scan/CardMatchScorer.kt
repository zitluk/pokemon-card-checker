package com.pokemontcg.collection.data.scan

import com.pokemontcg.collection.data.remote.CardBriefDto

object CardMatchScorer {
    fun score(card: CardBriefDto, extraction: ScanExtraction, queryUsed: String?): Int {
        val nameLower = card.name.lowercase()
        val blob = extraction.rawPreview.lowercase()
        var score = 40

        if (queryUsed != null && nameLower.contains(queryUsed.lowercase())) {
            score += 35
        }
        extraction.searchQueries.firstOrNull()?.let { q ->
            val ql = q.lowercase()
            when {
                nameLower == ql -> score += 40
                nameLower.contains(ql) || ql.contains(nameLower) -> score += 25
                tokenOverlap(nameLower, ql) >= 2 -> score += 15
            }
        }
        extraction.tcgLocalId?.let { hint ->
            val cardLocal = normalizeLocalId(card.localId)
            val want = normalizeLocalId(hint)
            if (cardLocal.isNotEmpty() && cardLocal == want) {
                score += 35
            }
        }
        extraction.cardNumberHint?.let { hint ->
            val normalizedHint = hint.substringBefore("/").let { normalizeLocalId(it) }
            val local = normalizeLocalId(card.localId)
            if (local.isNotEmpty() && local == normalizedHint) {
                score += 25
            }
        }
        extraction.setHints.forEach { setId ->
            if (card.id.startsWith("$setId-", ignoreCase = true)) {
                score += 30
            }
        }
        if (blob.isNotBlank() && tokenOverlap(nameLower, blob) >= 1) {
            score += 10
        }
        return score.coerceIn(0, 100)
    }

    fun pickBest(
        cards: List<CardBriefDto>,
        extraction: ScanExtraction,
        queryUsed: String?,
    ): Pair<CardBriefDto, Int>? {
        if (cards.isEmpty()) return null
        return cards
            .map { it to score(it, extraction, queryUsed) }
            .maxByOrNull { it.second }
    }

    private fun normalizeLocalId(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val digits = raw.trim().substringBefore("/").filter { it.isDigit() }
        if (digits.isEmpty()) return raw.trim().lowercase()
        return digits.trimStart('0').ifEmpty { "0" }.padStart(3, '0')
    }

    private fun tokenOverlap(a: String, b: String): Int {
        val ta = a.split(Regex("""\s+""")).filter { it.length >= 3 }.toSet()
        val tb = b.split(Regex("""\s+""")).filter { it.length >= 3 }.toSet()
        return ta.intersect(tb).size
    }
}
