package com.pokemontcg.collection.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TcgDexApi {
    @GET("en/cards")
    suspend fun searchCards(@Query("name") name: String): List<CardBriefDto>

    @GET("en/cards/{id}")
    suspend fun getCard(@Path("id") id: String): CardDetailDto
}

@JsonClass(generateAdapter = true)
data class CardBriefDto(
    val id: String,
    val name: String,
    val image: String? = null,
    @Json(name = "localId") val localId: String? = null,
)

@JsonClass(generateAdapter = true)
data class CardDetailDto(
    val id: String,
    val name: String,
    val image: String? = null,
    @Json(name = "localId") val localId: String? = null,
    val rarity: String? = null,
    val set: CardSetDto? = null,
    @Json(name = "variants_detailed") val variantsDetailed: List<VariantDetailedDto>? = null,
)

@JsonClass(generateAdapter = true)
data class CardSetDto(
    val id: String? = null,
    val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class VariantDetailedDto(
    val pricing: VariantPricingDto? = null,
)

@JsonClass(generateAdapter = true)
data class VariantPricingDto(
    val cardmarket: CardmarketPricingDto? = null,
)

@JsonClass(generateAdapter = true)
data class CardmarketPricingDto(
    val updated: String? = null,
    val unit: String? = null,
    val trend: Double? = null,
    val avg: Double? = null,
    val low: Double? = null,
)
