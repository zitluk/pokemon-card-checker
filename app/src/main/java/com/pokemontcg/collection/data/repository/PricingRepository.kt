package com.pokemontcg.collection.data.repository

import com.pokemontcg.collection.data.local.CardCacheDao
import com.pokemontcg.collection.data.model.CardSummary

class PricingRepository(
    private val cardCacheDao: CardCacheDao,
    private val catalogRepository: CatalogRepository,
) {
    suspend fun refreshPricesForCollection(cardIds: List<String>): Int {
        var updated = 0
        for (id in cardIds.distinct()) {
            val result = catalogRepository.refreshCardCache(id)
            if (result.isSuccess) updated++
        }
        return updated
    }

    suspend fun getCachedSummary(cardId: String): CardSummary? =
        cardCacheDao.get(cardId)?.let { cache ->
            catalogRepository.getCardDetail(cardId).getOrNull()
        }
}
