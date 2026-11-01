package com.pokemontcg.collection.data.repository

import com.pokemontcg.collection.data.local.CardCacheDao
import com.pokemontcg.collection.data.local.CardCacheEntity
import com.pokemontcg.collection.data.local.SearchHistoryDao
import com.pokemontcg.collection.data.local.SearchHistoryEntity
import com.pokemontcg.collection.data.model.CardSummary
import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.remote.CardDetailDto
import com.pokemontcg.collection.data.remote.CardmarketPricingDto
import com.pokemontcg.collection.data.remote.TcgDexApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class CatalogRepository(
    private val api: TcgDexApi,
    private val cardCacheDao: CardCacheDao,
    private val searchHistoryDao: SearchHistoryDao,
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val detailAdapter = moshi.adapter(CardDetailDto::class.java)
    private val pricingAdapter = moshi.adapter(CardmarketPricingDto::class.java)

    fun observeRecentSearches(): Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.observeRecent()

    suspend fun searchCards(query: String): Result<List<CardBriefDto>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return Result.success(emptyList())
        return try {
            val results = api.searchCards(trimmed)
            searchHistoryDao.insert(
                SearchHistoryEntity(query = trimmed, searchedAt = System.currentTimeMillis()),
            )
            Result.success(results)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }

    suspend fun getCardDetail(cardId: String, forceRefresh: Boolean = false): Result<CardSummary> {
        val cached = cardCacheDao.get(cardId)
        if (!forceRefresh && cached != null && !PricingHelper.isStale(cached.pricingUpdatedAt)) {
            return Result.success(cached.toSummary())
        }
        return try {
            val detail = api.getCard(cardId)
            val entity = detail.toCacheEntity()
            cardCacheDao.upsert(entity)
            Result.success(entity.toSummary())
        } catch (e: IOException) {
            if (cached != null) Result.success(cached.toSummary())
            else Result.failure(e)
        } catch (e: HttpException) {
            if (cached != null) Result.success(cached.toSummary())
            else Result.failure(e)
        }
    }

    suspend fun refreshCardCache(cardId: String): Result<CardSummary> =
        getCardDetail(cardId, forceRefresh = true)

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    suspend fun clearCardCache() {
        cardCacheDao.clearAll()
    }

    private fun CardDetailDto.toCacheEntity(): CardCacheEntity {
        val pricing = PricingHelper.extractBestCardmarket(this)
        val pricingJson = pricing?.let { pricingAdapter.toJson(it) }
        val imageUrl = image?.let { if (it.endsWith("/")) "${it}high.webp" else "$it/high.webp" }
        return CardCacheEntity(
            tcgdexCardId = id,
            name = name,
            setName = set?.name,
            localId = localId,
            rarity = rarity,
            imageUrl = imageUrl,
            jsonSnapshot = detailAdapter.toJson(this),
            pricingJson = pricingJson,
            pricingUpdatedAt = PricingHelper.parseUpdatedAt(pricing?.updated),
            cachedAt = System.currentTimeMillis(),
        )
    }

    private fun CardCacheEntity.toSummary(): CardSummary {
        val price = pricingJson?.let { runCatching { pricingAdapter.fromJson(it) }.getOrNull() }
        val priceEur = price?.trend ?: price?.avg ?: price?.low
        return CardSummary(
            id = tcgdexCardId,
            name = name,
            setName = setName,
            localId = localId,
            rarity = rarity,
            imageUrl = imageUrl,
            priceEur = priceEur,
            priceUpdatedAt = pricingUpdatedAt,
            isPriceStale = PricingHelper.isStale(pricingUpdatedAt),
        )
    }
}
