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
        extraction.cardNumberHint?.let { hint ->
            val normalizedHint = hint.replace(" ", "")
            val local = card.localId?.replace(" ", "") ?: ""
            if (local.isNotEmpty() && (local == normalizedHint || local.endsWith(normalizedHint))) {
                score += 25
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

    private fun tokenOverlap(a: String, b: String): Int {
        val ta = a.split(Regex("""\s+""")).filter { it.length >= 3 }.toSet()
        val tb = b.split(Regex("""\s+""")).filter { it.length >= 3 }.toSet()
        return ta.intersect(tb).size
    }
}
