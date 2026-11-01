package com.pokemontcg.collection.data.repository

import com.pokemontcg.collection.data.remote.CardDetailDto
import com.pokemontcg.collection.data.remote.CardmarketPricingDto
import java.time.Instant
import java.time.format.DateTimeParseException

object PricingHelper {
    private const val STALE_MS = 24 * 60 * 60 * 1000L

    fun extractBestCardmarket(detail: CardDetailDto): CardmarketPricingDto? =
        detail.variantsDetailed
            ?.mapNotNull { it.pricing?.cardmarket }
            ?.maxByOrNull { it.trend ?: it.avg ?: it.low ?: 0.0 }

    fun parseUpdatedAt(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        return try {
            Instant.parse(iso).toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun isStale(updatedAt: Long?): Boolean {
        if (updatedAt == null) return true
        return System.currentTimeMillis() - updatedAt > STALE_MS
    }
}
