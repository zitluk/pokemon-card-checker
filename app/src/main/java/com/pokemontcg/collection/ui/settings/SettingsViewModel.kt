package com.pokemontcg.collection.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.BuildConfig
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.SettingsRepository
import com.pokemontcg.collection.data.update.UpdateCheckOutcome
import com.pokemontcg.collection.data.update.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val catalogRepository: CatalogRepository,
    private val updateChecker: UpdateChecker,
) : ViewModel() {
    val darkTheme = settingsRepository.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val appVersionName: String = BuildConfig.VERSION_NAME

    private val _updateState = MutableStateFlow<SettingsUpdateUiState>(SettingsUpdateUiState.Idle)
    val updateState: StateFlow<SettingsUpdateUiState> = _updateState.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun clearCaches() {
        viewModelScope.launch { settingsRepository.clearCaches(catalogRepository) }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = SettingsUpdateUiState.Loading
            updateChecker.checkLatestRelease(appVersionName)
                .onSuccess { outcome ->
                    _updateState.value = when (outcome) {
                        is UpdateCheckOutcome.UpToDate ->
                            SettingsUpdateUiState.UpToDate
                        is UpdateCheckOutcome.UpdateAvailable ->
                            SettingsUpdateUiState.UpdateAvailable(
                                versionLabel = outcome.remoteVersionLabel,
                                releaseUrl = outcome.releaseUrl,
                            )
                    }
                }
                .onFailure { e ->
                    _updateState.value = when (e) {
                        is UpdateChecker.NoReleaseException ->
                            SettingsUpdateUiState.Error(UpdateErrorKind.NO_RELEASES)
                        is IOException ->
                            SettingsUpdateUiState.Error(UpdateErrorKind.OFFLINE)
                        else ->
                            SettingsUpdateUiState.Error(UpdateErrorKind.OTHER)
                    }
                }
        }
    }
}
