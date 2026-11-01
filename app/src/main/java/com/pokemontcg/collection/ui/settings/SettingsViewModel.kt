package com.pokemontcg.collection.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    val darkTheme = settingsRepository.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun clearCaches() {
        viewModelScope.launch { settingsRepository.clearCaches(catalogRepository) }
    }
}
