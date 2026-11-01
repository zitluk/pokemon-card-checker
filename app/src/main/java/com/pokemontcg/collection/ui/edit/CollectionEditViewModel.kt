package com.pokemontcg.collection.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.data.model.CollectionItem
import com.pokemontcg.collection.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CollectionEditUiState(
    val item: CollectionItem? = null,
    val quantity: String = "1",
    val condition: CardCondition = CardCondition.NM,
    val language: String = "EN",
    val note: String = "",
    val purchasePrice: String = "",
    val saved: Boolean = false,
)

class CollectionEditViewModel(
    private val collectionRepository: CollectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionEditUiState())
    val uiState: StateFlow<CollectionEditUiState> = _uiState

    fun load(itemId: Long) {
        viewModelScope.launch {
            val item = collectionRepository.getItem(itemId) ?: return@launch
            _uiState.value = CollectionEditUiState(
                item = item,
                quantity = item.quantity.toString(),
                condition = item.condition,
                language = item.language,
                note = item.note,
                purchasePrice = item.purchasePriceEur?.toString() ?: "",
            )
        }
    }

    fun loadForCard(cardId: String) {
        viewModelScope.launch {
            val item = collectionRepository.getItemForCard(cardId)
            if (item != null) {
                load(item.id)
            } else {
                _uiState.value = CollectionEditUiState()
            }
        }
    }

    fun updateQuantity(value: String) {
        _uiState.value = _uiState.value.copy(quantity = value.filter { it.isDigit() }.ifEmpty { "1" })
    }

    fun updateCondition(condition: CardCondition) {
        _uiState.value = _uiState.value.copy(condition = condition)
    }

    fun updateLanguage(value: String) {
        _uiState.value = _uiState.value.copy(language = value)
    }

    fun updateNote(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun updatePurchasePrice(value: String) {
        _uiState.value = _uiState.value.copy(purchasePrice = value)
    }

    fun save(cardId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val qty = state.quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val purchase = state.purchasePrice.replace(',', '.').toDoubleOrNull()
            collectionRepository.upsert(
                cardId = cardId,
                quantity = qty,
                condition = state.condition,
                language = state.language.trim().ifEmpty { "EN" },
                note = state.note.trim(),
                purchasePriceEur = purchase,
                existingId = state.item?.id,
            )
            _uiState.value = state.copy(saved = true)
        }
    }
}
