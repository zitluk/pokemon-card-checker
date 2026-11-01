package com.pokemontcg.collection.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.data.model.CardSummary
import com.pokemontcg.collection.data.model.CollectionItem
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val card: CardSummary? = null,
    val collectionItem: CollectionItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class CardDetailViewModel(
    private val catalogRepository: CatalogRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState

    fun load(cardId: String) {
        viewModelScope.launch {
            _uiState.value = CardDetailUiState(isLoading = true)
            val cardResult = catalogRepository.getCardDetail(cardId)
            val collectionItem = collectionRepository.getItemForCard(cardId)
            cardResult
                .onSuccess { card ->
                    _uiState.value = CardDetailUiState(
                        card = card,
                        collectionItem = collectionItem,
                        isLoading = false,
                    )
                }
                .onFailure {
                    _uiState.value = CardDetailUiState(
                        isLoading = false,
                        error = it.message,
                        collectionItem = collectionItem,
                    )
                }
        }
    }

    fun refreshPrice(cardId: String) {
        viewModelScope.launch {
            catalogRepository.refreshCardCache(cardId)
                .onSuccess { card ->
                    _uiState.value = _uiState.value.copy(card = card)
                }
        }
    }

    suspend fun quickAdd(cardId: String) {
        catalogRepository.getCardDetail(cardId)
        collectionRepository.upsert(
            cardId = cardId,
            quantity = 1,
            condition = CardCondition.NM,
            language = "EN",
            note = "",
            purchasePriceEur = null,
            existingId = _uiState.value.collectionItem?.id,
        )
        _uiState.value = _uiState.value.copy(
            collectionItem = collectionRepository.getItemForCard(cardId),
        )
    }
}
