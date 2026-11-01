package com.pokemontcg.collection.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection_items")
data class CollectionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tcgdexCardId: String,
    val quantity: Int,
    val condition: String,
    val language: String,
    val note: String,
    val purchasePriceEur: Double?,
    val addedAt: Long,
)

@Entity(tableName = "cards_cache")
data class CardCacheEntity(
    @PrimaryKey val tcgdexCardId: String,
    val name: String,
    val setName: String?,
    val localId: String? = null,
    val rarity: String?,
    val imageUrl: String?,
    val jsonSnapshot: String,
    val pricingJson: String?,
    val pricingUpdatedAt: Long?,
    val cachedAt: Long,
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchedAt: Long,
)
