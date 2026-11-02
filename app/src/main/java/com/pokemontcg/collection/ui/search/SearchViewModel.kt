package com.pokemontcg.collection.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.remote.CardBriefDto
import com.pokemontcg.collection.data.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<CardBriefDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SearchViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

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

    fun clearQuery() {
        debounceJob?.cancel()
        _uiState.value = SearchUiState()
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
