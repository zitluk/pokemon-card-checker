package com.pokemontcg.collection.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[darkThemeKey] ?: true
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[darkThemeKey] = enabled
        }
    }

    suspend fun clearCaches(
        catalogRepository: CatalogRepository,
    ) {
        catalogRepository.clearCardCache()
        catalogRepository.clearSearchHistory()
    }
}
