package com.pokemontcg.collection.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.model.CollectionItem
import com.pokemontcg.collection.data.model.CollectionStats
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.CollectionRepository
import com.pokemontcg.collection.data.repository.PricingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionUiState(
    val items: List<CollectionItem> = emptyList(),
    val stats: CollectionStats = CollectionStats(0, 0, 0.0),
    val sort: CollectionSort = CollectionSort.DATE_ADDED,
    val isRefreshingPrices: Boolean = false,
    val priceRefreshCount: Int? = null,
    val userMessage: String? = null,
)

enum class CollectionSort {
    DATE_ADDED,
    NAME,
    SET,
    VALUE,
}

class CollectionViewModel(
    private val collectionRepository: CollectionRepository,
    private val pricingRepository: PricingRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val sort = MutableStateFlow(CollectionSort.DATE_ADDED)
    private val refreshing = MutableStateFlow(false)
    private val priceRefreshCount = MutableStateFlow<Int?>(null)
    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CollectionUiState> = combine(
        combine(
            collectionRepository.observeCollection(),
            collectionRepository.observeStats(),
            sort,
            refreshing,
            priceRefreshCount,
        ) { items, stats, currentSort, isRefreshing, refreshCount ->
            CollectionUiState(
                items = items,
                stats = stats,
                sort = currentSort,
                isRefreshingPrices = isRefreshing,
                priceRefreshCount = refreshCount,
            )
        },
        userMessage,
    ) { base, message ->
        val sorted = when (base.sort) {
            CollectionSort.NAME -> base.items.sortedBy { it.cardName.lowercase() }
            CollectionSort.SET -> base.items.sortedBy { it.setName?.lowercase() ?: "" }
            CollectionSort.VALUE -> base.items.sortedByDescending { (it.marketPriceEur ?: 0.0) * it.quantity }
            CollectionSort.DATE_ADDED -> base.items.sortedByDescending { it.addedAt }
        }
        base.copy(items = sorted, userMessage = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    fun setSort(newSort: CollectionSort) {
        sort.value = newSort
    }

    fun refreshPrices() {
        viewModelScope.launch {
            refreshing.value = true
            val ids = uiState.value.items.map { it.cardId }
            val count = pricingRepository.refreshPricesForCollection(ids)
            priceRefreshCount.value = count
            refreshing.value = false
        }
    }

    fun consumePriceRefreshMessage() {
        priceRefreshCount.value = null
    }

    fun showUserMessage(message: String) {
        userMessage.value = message
    }

    fun consumeUserMessage() {
        userMessage.value = null
    }

    fun preloadCache(cardId: String) {
        viewModelScope.launch {
            catalogRepository.getCardDetail(cardId)
        }
    }
}
