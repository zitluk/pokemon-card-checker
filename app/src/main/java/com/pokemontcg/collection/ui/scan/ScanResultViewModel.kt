package com.pokemontcg.collection.ui.scan

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.data.model.CardSummary
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.CollectionRepository
import com.pokemontcg.collection.data.scan.AuthenticityEstimator
import com.pokemontcg.collection.data.scan.CardMatchScorer
import com.pokemontcg.collection.data.scan.CardScanRecognizer
import com.pokemontcg.collection.data.scan.ScanCaptureStorage
import com.pokemontcg.collection.data.scan.ScanCardBackDetector
import com.pokemontcg.collection.data.scan.ScanImagePreprocessor
import com.pokemontcg.collection.data.scan.ScanQueryExtractor
import com.pokemontcg.collection.data.scan.ScanSearchPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

enum class ScanResultError {
    NO_TEXT,
    NO_MATCH,
    OFFLINE,
    RECOGNITION_FAILED,
    MISSING_FILE,
    CARD_BACK,
}

data class ScanResultUiState(
    val isLoading: Boolean = true,
    val card: CardSummary? = null,
    val authenticityScore: Int? = null,
    val usedQuery: String? = null,
    val queriesTried: List<String> = emptyList(),
    val ocrPreview: String? = null,
    val error: ScanResultError? = null,
    val fromCacheOnly: Boolean = false,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
)

class ScanResultViewModel(
    application: Application,
    private val catalogRepository: CatalogRepository,
    private val collectionRepository: CollectionRepository,
) : AndroidViewModel(application) {
    private val recognizer = CardScanRecognizer(application)
    private var capturePath: String? = null

    private val _uiState = MutableStateFlow(ScanResultUiState())
    val uiState: StateFlow<ScanResultUiState> = _uiState

    fun analyze(capturePath: String) {
        if (this.capturePath == capturePath && !_uiState.value.isLoading && _uiState.value.card != null) {
            return
        }
        this.capturePath = capturePath
        viewModelScope.launch {
            _uiState.value = ScanResultUiState(isLoading = true)
            val file = File(capturePath)
            if (!file.exists()) {
                _uiState.value = ScanResultUiState(isLoading = false, error = ScanResultError.MISSING_FILE)
                return@launch
            }
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(capturePath, bounds)
                val aspectScore = AuthenticityEstimator.aspectRatioScore(bounds.outWidth, bounds.outHeight)

                val ocrBitmaps = ScanImagePreprocessor.ocrPassBitmaps(file)
                val ocr = if (ocrBitmaps.isEmpty()) {
                    val inputImage = ScanImagePreprocessor.inputImageForOcr(getApplication(), file)
                    recognizer.recognizeDetailed(inputImage)
                } else {
                    recognizer.recognizeMergedFromBitmaps(ocrBitmaps)
                }
                val extraction = ScanQueryExtractor.extract(ocr.fullText)

                if (ScanCardBackDetector.isLikelyCardBack(ocr.fullText, extraction)) {
                    _uiState.value = ScanResultUiState(
                        isLoading = false,
                        error = ScanResultError.CARD_BACK,
                        ocrPreview = ocrPreviewFor(extraction, ocr.fullText),
                    )
                    return@launch
                }

                val hasSearchSignals = extraction.searchQueries.isNotEmpty() ||
                    extraction.fallbackTokens.isNotEmpty() ||
                    extraction.tcgLocalId != null
                if (!hasSearchSignals) {
                    _uiState.value = ScanResultUiState(
                        isLoading = false,
                        error = ScanResultError.NO_TEXT,
                        ocrPreview = ocrPreviewFor(extraction, ocr.fullText),
                    )
                    return@launch
                }

                val searchOutcome = ScanSearchPipeline.searchCatalog(catalogRepository, extraction)
                if (searchOutcome.networkFailed) {
                    _uiState.value = ScanResultUiState(
                        isLoading = false,
                        error = ScanResultError.OFFLINE,
                        ocrPreview = ocrPreviewFor(extraction, ocr.fullText),
                        queriesTried = searchOutcome.queriesTried,
                    )
                    return@launch
                }

                val found = searchOutcome.cards
                val queryUsed = searchOutcome.queryUsed

                if (found.isEmpty()) {
                    _uiState.value = ScanResultUiState(
                        isLoading = false,
                        error = ScanResultError.NO_MATCH,
                        ocrPreview = ocrPreviewFor(extraction, ocr.fullText),
                        queriesTried = searchOutcome.queriesTried,
                    )
                    return@launch
                }

                val (best, matchScore) = CardMatchScorer.pickBest(found, extraction, queryUsed)
                    ?: (found.first() to 50)
                val detailResult = catalogRepository.getCardDetail(best.id)
                var fromCacheOnly = false
                detailResult.onFailure {
                    if (it is IOException) fromCacheOnly = true
                }
                val summary = detailResult.getOrNull()
                if (summary == null) {
                    _uiState.value = ScanResultUiState(
                        isLoading = false,
                        error = ScanResultError.OFFLINE,
                    )
                    return@launch
                }

                val card = summary.copy(localId = summary.localId ?: best.localId)
                val authenticity = AuthenticityEstimator.estimate(ocr, matchScore, aspectScore)

                _uiState.value = ScanResultUiState(
                    isLoading = false,
                    card = card,
                    authenticityScore = authenticity,
                    usedQuery = queryUsed,
                    ocrPreview = ocrPreviewFor(extraction, ocr.fullText),
                    fromCacheOnly = fromCacheOnly || summary.isPriceStale,
                )
            } catch (_: Exception) {
                _uiState.value = ScanResultUiState(isLoading = false, error = ScanResultError.RECOGNITION_FAILED)
            }
        }
    }

    private fun ocrPreviewFor(extraction: com.pokemontcg.collection.data.scan.ScanExtraction, fullText: String): String? {
        val preview = extraction.rawPreview.ifBlank {
            fullText.lines().map { it.trim() }.filter { it.length >= 2 }.take(4).joinToString(" · ")
        }
        return preview.ifBlank { null }
    }

    fun saveToCollection() {
        val cardId = _uiState.value.card?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            catalogRepository.getCardDetail(cardId)
            collectionRepository.upsert(
                cardId = cardId,
                quantity = 1,
                condition = CardCondition.NM,
                language = "EN",
                note = "",
                purchasePriceEur = null,
            )
            ScanCaptureStorage.deleteIfExists(capturePath)
            capturePath = null
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }

    fun discardCapture() {
        ScanCaptureStorage.deleteIfExists(capturePath)
        capturePath = null
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
