package com.pokemontcg.collection.data.repository

import com.pokemontcg.collection.data.local.CardCacheDao
import com.pokemontcg.collection.data.local.CardCacheEntity
import com.pokemontcg.collection.data.local.SearchHistoryDao
import com.pokemontcg.collection.data.model.CardSummary
import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.remote.CardDetailDto
import com.pokemontcg.collection.data.remote.CardmarketPricingDto
import com.pokemontcg.collection.data.remote.TcgDexApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    suspend fun searchCards(query: String): Result<List<CardBriefDto>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return Result.success(emptyList())
        val primary = executeNameSearch(trimmed)
        if (primary.isSuccess && primary.getOrNull().orEmpty().isNotEmpty()) return primary
        if (trimmed.length >= 3 && !trimmed.startsWith("like:", ignoreCase = true)) {
            val fuzzy = executeNameSearch("like:$trimmed")
            if (fuzzy.isSuccess && fuzzy.getOrNull().orEmpty().isNotEmpty()) return fuzzy
        }
        return primary
    }

    suspend fun searchByLocalId(localId: String, setId: String? = null): Result<List<CardBriefDto>> =
        executeQuery {
            api.queryCards(name = null, localId = localId, setId = setId)
        }

    suspend fun searchByNameAndSet(name: String, setId: String): Result<List<CardBriefDto>> =
        executeQuery {
            api.queryCards(name = name, localId = null, setId = setId)
        }

    suspend fun fetchCardBriefById(cardId: String): Result<CardBriefDto?> =
        try {
            val detail = api.getCard(cardId)
            Result.success(
                CardBriefDto(
                    id = detail.id,
                    name = detail.name,
                    image = detail.image,
                    localId = detail.localId,
                ),
            )
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            if (e.code() == 404) Result.success(null) else Result.failure(e)
        }

    private suspend fun executeNameSearch(query: String): Result<List<CardBriefDto>> =
        executeQuery { api.searchCards(query) }

    private suspend fun executeQuery(block: suspend () -> List<CardBriefDto>): Result<List<CardBriefDto>> =
        try {
            Result.success(block())
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
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
