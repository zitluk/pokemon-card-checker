package com.pokemontcg.collection.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pokemontcg.collection.data.AppContainer
import com.pokemontcg.collection.ui.collection.CollectionViewModel
import com.pokemontcg.collection.ui.detail.CardDetailViewModel
import com.pokemontcg.collection.ui.edit.CollectionEditViewModel
import com.pokemontcg.collection.ui.search.SearchViewModel
import com.pokemontcg.collection.ui.scan.ScanResultViewModel
import com.pokemontcg.collection.ui.scan.ScanViewModel
import com.pokemontcg.collection.ui.settings.SettingsViewModel
import android.app.Application

class AppViewModelFactory(
    private val container: AppContainer,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(CollectionViewModel::class.java) ->
            CollectionViewModel(
                container.collectionRepository,
                container.pricingRepository,
                container.catalogRepository,
            ) as T
        modelClass.isAssignableFrom(SearchViewModel::class.java) ->
            SearchViewModel(container.catalogRepository) as T
        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(container.settingsRepository, container.catalogRepository) as T
        modelClass.isAssignableFrom(CardDetailViewModel::class.java) ->
            CardDetailViewModel(
                container.catalogRepository,
                container.collectionRepository,
            ) as T
        modelClass.isAssignableFrom(CollectionEditViewModel::class.java) ->
            CollectionEditViewModel(container.collectionRepository) as T
        modelClass.isAssignableFrom(ScanViewModel::class.java) ->
            ScanViewModel(application) as T
        modelClass.isAssignableFrom(ScanResultViewModel::class.java) ->
            ScanResultViewModel(
                application,
                container.catalogRepository,
                container.collectionRepository,
            ) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
