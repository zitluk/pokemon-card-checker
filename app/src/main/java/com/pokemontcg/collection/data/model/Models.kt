package com.pokemontcg.collection.data.model

data class CardSummary(
    val id: String,
    val name: String,
    val setName: String?,
    val localId: String? = null,
    val rarity: String?,
    val imageUrl: String?,
    val priceEur: Double?,
    val priceUpdatedAt: Long?,
    val isPriceStale: Boolean,
)

data class CollectionItem(
    val id: Long,
    val cardId: String,
    val cardName: String,
    val setName: String?,
    val imageUrl: String?,
    val quantity: Int,
    val condition: CardCondition,
    val language: String,
    val note: String,
    val purchasePriceEur: Double?,
    val marketPriceEur: Double?,
    val priceUpdatedAt: Long?,
    val addedAt: Long,
)

enum class CardCondition {
    NM,
    LP,
    MP,
    HP,
    DMG,
    ;

    companion object {
        fun fromStored(value: String): CardCondition =
            entries.find { it.name == value } ?: NM
    }
}

data class CollectionStats(
    val totalCards: Int,
    val distinctEntries: Int,
    val estimatedValueEur: Double,
)
