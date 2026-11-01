package com.pokemontcg.collection.data.repository

import com.pokemontcg.collection.data.local.CardCacheDao
import com.pokemontcg.collection.data.local.CollectionDao
import com.pokemontcg.collection.data.local.CollectionItemEntity
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.data.model.CollectionItem
import com.pokemontcg.collection.data.model.CollectionStats
import com.pokemontcg.collection.data.remote.CardmarketPricingDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val cardCacheDao: CardCacheDao,
) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun observeCollection(): Flow<List<CollectionItem>> =
        collectionDao.observeAll().map { items ->
            items.map { it.toModel() }
        }

    fun observeStats(): Flow<CollectionStats> =
        observeCollection().map { items ->
            val totalQty = items.sumOf { it.quantity }
            val value = items.sumOf { (it.marketPriceEur ?: 0.0) * it.quantity }
            CollectionStats(
                totalCards = totalQty,
                distinctEntries = items.size,
                estimatedValueEur = value,
            )
        }

    suspend fun getItem(id: Long): CollectionItem? =
        collectionDao.getById(id)?.toModel()

    suspend fun getItemForCard(cardId: String): CollectionItem? =
        collectionDao.getByCardId(cardId)?.toModel()

    suspend fun upsert(
        cardId: String,
        quantity: Int,
        condition: CardCondition,
        language: String,
        note: String,
        purchasePriceEur: Double?,
        existingId: Long? = null,
    ): Long {
        val now = System.currentTimeMillis()
        val entity = if (existingId != null) {
            collectionDao.getById(existingId)?.copy(
                quantity = quantity,
                condition = condition.name,
                language = language,
                note = note,
                purchasePriceEur = purchasePriceEur,
            ) ?: CollectionItemEntity(
                id = existingId,
                tcgdexCardId = cardId,
                quantity = quantity,
                condition = condition.name,
                language = language,
                note = note,
                purchasePriceEur = purchasePriceEur,
                addedAt = now,
            )
        } else {
            CollectionItemEntity(
                tcgdexCardId = cardId,
                quantity = quantity,
                condition = condition.name,
                language = language,
                note = note,
                purchasePriceEur = purchasePriceEur,
                addedAt = now,
            )
        }
        return collectionDao.upsert(entity)
    }

    suspend fun delete(id: Long) {
        collectionDao.delete(id)
    }

    private suspend fun CollectionItemEntity.toModel(): CollectionItem {
        val cache = cardCacheDao.get(tcgdexCardId)
        val price = cache?.pricingJson?.let {
            runCatching {
                moshi.adapter(CardmarketPricingDto::class.java).fromJson(it)
            }.getOrNull()
        }
        val priceEur = price?.trend ?: price?.avg ?: price?.low
        return CollectionItem(
            id = id,
            cardId = tcgdexCardId,
            cardName = cache?.name ?: tcgdexCardId,
            setName = cache?.setName,
            imageUrl = cache?.imageUrl,
            quantity = quantity,
            condition = CardCondition.fromStored(condition),
            language = language,
            note = note,
            purchasePriceEur = purchasePriceEur,
            marketPriceEur = priceEur,
            priceUpdatedAt = cache?.pricingUpdatedAt,
            addedAt = addedAt,
        )
    }
}
