package com.pokemontcg.collection.data.scan

import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.repository.CatalogRepository
import java.io.IOException

data class ScanSearchOutcome(
    val cards: List<CardBriefDto>,
    val queryUsed: String?,
    val hadOcrQueries: Boolean,
    val networkFailed: Boolean,
)

object ScanSearchPipeline {
    suspend fun searchCatalog(
        catalogRepository: CatalogRepository,
        extraction: ScanExtraction,
    ): ScanSearchOutcome {
        val queries = buildQueryList(extraction)
        if (queries.isEmpty()) {
            return ScanSearchOutcome(
                cards = emptyList(),
                queryUsed = null,
                hadOcrQueries = false,
                networkFailed = false,
            )
        }

        val merged = linkedMapOf<String, CardBriefDto>()
        var queryUsed: String? = null
        var networkFailed = false

        for (query in queries) {
            val result = catalogRepository.searchCards(query)
            result.onSuccess { cards ->
                if (cards.isNotEmpty()) {
                    if (queryUsed == null) queryUsed = query
                    cards.forEach { merged[it.id] = it }
                }
            }.onFailure { e ->
                if (e is IOException) networkFailed = true
            }
        }

        return ScanSearchOutcome(
            cards = merged.values.toList(),
            queryUsed = queryUsed,
            hadOcrQueries = true,
            networkFailed = networkFailed && merged.isEmpty(),
        )
    }

    private fun buildQueryList(extraction: ScanExtraction): List<String> {
        val list = linkedSetOf<String>()
        list.addAll(extraction.searchQueries)
        extraction.cardNumberHint?.let { hint ->
            val parts = hint.split("/")
            if (parts.size == 2) {
                list.add(parts[0].trim())
            }
        }
        extraction.fallbackTokens.forEach { list.add(it) }
        return list
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
            .take(12)
    }
}
