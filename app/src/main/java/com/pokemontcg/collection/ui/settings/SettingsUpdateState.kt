package com.pokemontcg.collection.ui.settings

sealed interface SettingsUpdateUiState {
    data object Idle : SettingsUpdateUiState
    data object Loading : SettingsUpdateUiState
    data object UpToDate : SettingsUpdateUiState
    data class UpdateAvailable(
        val versionLabel: String,
        val releaseUrl: String,
    ) : SettingsUpdateUiState
    data class Error(val kind: UpdateErrorKind) : SettingsUpdateUiState
}

enum class UpdateErrorKind {
    OFFLINE,
    NO_RELEASES,
    OTHER,
}
