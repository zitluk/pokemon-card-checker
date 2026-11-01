package com.pokemontcg.collection.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pokemontcg.collection.data.scan.CardScanRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ScanUiState(
    val hasCameraPermission: Boolean = false,
    val isCapturing: Boolean = false,
    val pendingCapturePath: String? = null,
    val snackbarMessage: String? = null,
)

class ScanViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val recognizer = CardScanRecognizer(application)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun onCaptureStarted() {
        _uiState.update { it.copy(isCapturing = true, pendingCapturePath = null) }
    }

    fun onPhotoSaved(path: String) {
        _uiState.update {
            it.copy(isCapturing = false, pendingCapturePath = path)
        }
    }

    fun onCaptureFailed() {
        _uiState.update { it.copy(isCapturing = false) }
    }

    fun consumePendingCapturePath(): String? {
        var path: String? = null
        _uiState.update {
            path = it.pendingCapturePath
            it.copy(pendingCapturePath = null)
        }
        return path
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
