package com.pokemontcg.collection.data.scan

import android.util.Log
import com.pokemontcg.collection.BuildConfig
import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.repository.CatalogRepository
import java.io.IOException

data class ScanSearchOutcome(
    val cards: List<CardBriefDto>,
    val queryUsed: String?,
    val queriesTried: List<String>,
    val hadOcrQueries: Boolean,
    val networkFailed: Boolean,
)

object ScanSearchPipeline {
    private const val TAG = "ScanSearch"

    suspend fun searchCatalog(
        catalogRepository: CatalogRepository,
        extraction: ScanExtraction,
    ): ScanSearchOutcome {
        val steps = buildSearchSteps(extraction)
        if (steps.isEmpty()) {
            return ScanSearchOutcome(
                cards = emptyList(),
                queryUsed = null,
                queriesTried = emptyList(),
                hadOcrQueries = false,
                networkFailed = false,
            )
        }

        val merged = linkedMapOf<String, CardBriefDto>()
        var queryUsed: String? = null
        var networkFailed = false
        val tried = mutableListOf<String>()

        for (step in steps) {
            tried.add(step.label)
            if (BuildConfig.DEBUG) Log.d(TAG, "try: ${step.label}")
            when (step) {
                is ScanSearchStep.ByCardId -> {
                    catalogRepository.fetchCardBriefById(step.cardId)
                        .onSuccess { card ->
                            if (card != null) {
                                if (queryUsed == null) queryUsed = step.label
                                merged[card.id] = card
                            }
                        }
                        .onFailure { e -> markFailure(e, step.label) { networkFailed = true } }
                }
                is ScanSearchStep.ByLocalId -> {
                    catalogRepository.searchByLocalId(step.localId, step.setId)
                        .onSuccess { cards -> absorb(merged, cards, step.label) { queryUsed = queryUsed ?: it } }
                        .onFailure { e -> markFailure(e, step.label) { networkFailed = true } }
                }
                is ScanSearchStep.ByName -> {
                    catalogRepository.searchCards(step.name)
                        .onSuccess { cards -> absorb(merged, cards, step.label) { queryUsed = queryUsed ?: it } }
                        .onFailure { e -> markFailure(e, step.label) { networkFailed = true } }
                }
                is ScanSearchStep.ByNameInSet -> {
                    catalogRepository.searchByNameAndSet(step.name, step.setId)
                        .onSuccess { cards -> absorb(merged, cards, step.label) { queryUsed = queryUsed ?: it } }
                        .onFailure { e -> markFailure(e, step.label) { networkFailed = true } }
                }
            }
        }

        return ScanSearchOutcome(
            cards = merged.values.toList(),
            queryUsed = queryUsed,
            queriesTried = tried.distinct().take(8),
            hadOcrQueries = true,
            networkFailed = networkFailed && merged.isEmpty(),
        )
    }

    private sealed interface ScanSearchStep {
        val label: String

        data class ByLocalId(val localId: String, val setId: String?, override val label: String) : ScanSearchStep
        data class ByName(val name: String, override val label: String) : ScanSearchStep
        data class ByNameInSet(val name: String, val setId: String, override val label: String) : ScanSearchStep
        data class ByCardId(val cardId: String, override val label: String) : ScanSearchStep
    }

    private fun buildSearchSteps(extraction: ScanExtraction): List<ScanSearchStep> {
        val steps = mutableListOf<ScanSearchStep>()
        val localId = extraction.tcgLocalId
        val setHints = extraction.setHints

        if (localId != null) {
            for (setId in setHints) {
                steps.add(
                    ScanSearchStep.ByLocalId(
                        localId = localId,
                        setId = setId,
                        label = "localId=$localId set=$setId",
                    ),
                )
                steps.add(
                    ScanSearchStep.ByCardId(
                        cardId = SetHintResolver.tcgDexCardId(setId, localId),
                        label = "id=${SetHintResolver.tcgDexCardId(setId, localId)}",
                    ),
                )
            }
            steps.add(
                ScanSearchStep.ByLocalId(
                    localId = localId,
                    setId = null,
                    label = "localId=$localId",
                ),
            )
        }

        extraction.searchQueries.forEach { q ->
            when {
                q.contains("-") && q.matches(Regex("""[a-z0-9]+-\d{3}""", RegexOption.IGNORE_CASE)) -> {
                    steps.add(ScanSearchStep.ByCardId(q.lowercase(), "id=$q"))
                }
                setHints.isNotEmpty() && q.contains("ex", ignoreCase = true) -> {
                    setHints.forEach { setId ->
                        steps.add(ScanSearchStep.ByNameInSet(q, setId, "name=$q set=$setId"))
                    }
                    steps.add(ScanSearchStep.ByName(q, "name=$q"))
                }
                else -> steps.add(ScanSearchStep.ByName(q, "name=$q"))
            }
        }

        extraction.fallbackTokens.forEach { token ->
            steps.add(ScanSearchStep.ByName(token, "token=$token"))
        }

        return steps
            .distinctBy { it.label }
            .take(16)
    }

    private fun absorb(
        merged: LinkedHashMap<String, CardBriefDto>,
        cards: List<CardBriefDto>,
        label: String,
        onFirstHit: (String) -> Unit,
    ) {
        if (cards.isEmpty()) return
        onFirstHit(label)
        cards.forEach { merged[it.id] = it }
    }

    private fun markFailure(e: Throwable, label: String, onNetwork: () -> Unit) {
        if (e is IOException) onNetwork()
        if (BuildConfig.DEBUG) Log.w(TAG, "fail $label: ${e.message}")
    }
}
