package com.pokemontcg.collection.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.local.SearchHistoryEntity
import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<CardBriefDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val recent: List<SearchHistoryEntity> = emptyList(),
)

class SearchViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    val recentSearches = catalogRepository.observeRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var debounceJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query, error = null)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(400)
            search(query)
        }
    }

    fun searchNow() {
        viewModelScope.launch { search(_uiState.value.query) }
    }

    private suspend fun search(query: String) {
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        catalogRepository.searchCards(query)
            .onSuccess { results ->
                _uiState.value = _uiState.value.copy(results = results, isLoading = false)
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message,
                )
            }
    }
}
