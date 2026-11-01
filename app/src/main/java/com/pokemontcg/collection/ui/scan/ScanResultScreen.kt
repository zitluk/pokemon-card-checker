package com.pokemontcg.collection.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.components.CardThumbnail
import com.pokemontcg.collection.ui.components.PriceTextOnCard
import com.pokemontcg.collection.ui.components.TcgCardBody
import com.pokemontcg.collection.ui.components.TcgCardTile
import com.pokemontcg.collection.ui.components.TcgCardTitle
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.layout.rememberResponsiveMetrics
import com.pokemontcg.collection.ui.theme.TcgChrome
import com.pokemontcg.collection.ui.theme.TcgColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    capturePath: String,
    viewModel: ScanResultViewModel,
    contentPadding: PaddingValues,
    onDiscard: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(capturePath) { viewModel.analyze(capturePath) }
    LaunchedEffect(state.saveComplete) {
        if (state.saveComplete) onSaved()
    }

    WatermarkedScreen(Modifier.fillMaxSize().padding(contentPadding)) {
        ResponsiveScreenScope {
            val metrics = rememberResponsiveMetrics()
            Scaffold(
                containerColor = TcgColors.PlaymatSurface.copy(alpha = 0f),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.scan_result_title)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.discardCapture()
                                onDiscard()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                )
                            }
                        },
                        colors = TcgChrome.topAppBarColors(),
                    )
                },
                bottomBar = {
                    if (state.card != null && !state.isLoading) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = metrics.horizontalPadding, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.discardCapture()
                                    onDiscard()
                                },
                                modifier = Modifier.weight(1f),
                                colors = TcgChrome.secondaryButtonColors(),
                                enabled = !state.isSaving,
                            ) {
                                Text(stringResource(R.string.scan_result_discard))
                            }
                            Button(
                                onClick = { viewModel.saveToCollection() },
                                modifier = Modifier.weight(1f),
                                colors = TcgChrome.primaryButtonColors(),
                                enabled = !state.isSaving,
                            ) {
                                Text(stringResource(R.string.scan_result_save))
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = metrics.horizontalPadding),
                ) {
                    when {
                        state.isLoading -> {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                CircularProgressIndicator(color = TcgColors.FrameGoldBright)
                                Text(
                                    stringResource(R.string.scan_result_loading),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        state.error != null -> {
                            Text(
                                scanResultErrorMessage(state.error!!),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = metrics.verticalSpacing),
                            )
                            state.ocrPreview?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TcgColors.OnPlaymatMuted,
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.discardCapture()
                                    onDiscard()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TcgChrome.primaryButtonColors(),
                            ) {
                                Text(stringResource(R.string.scan_result_back_to_scan))
                            }
                        }
                        else -> {
                            val card = state.card ?: return@Column
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                            ) {
                                if (state.fromCacheOnly) {
                                    Text(
                                        stringResource(R.string.scan_result_cache_partial),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TcgColors.FrameGoldBright,
                                    )
                                }
                                TcgCardTile {
                                    CardThumbnail(card.imageUrl, card.name)
                                    TcgCardTitle(card.name)
                                    TcgCardBody(
                                        card.setName ?: stringResource(R.string.collection_unknown_set),
                                        muted = true,
                                    )
                                    card.localId?.let {
                                        TcgCardBody(
                                            stringResource(R.string.scan_result_card_number, it),
                                            muted = true,
                                        )
                                    }
                                    card.rarity?.let { TcgCardBody(it) }
                                    PriceTextOnCard(card.priceEur, card.priceUpdatedAt, card.isPriceStale)
                                }
                                state.authenticityScore?.let { score ->
                                    TcgCardTile {
                                        Text(
                                            stringResource(R.string.scan_result_authenticity, score),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = TcgColors.CardInk,
                                        )
                                        LinearProgressIndicator(
                                            progress = { score / 100f },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = TcgColors.FrameGoldBright,
                                            trackColor = TcgColors.OnPlaymatMuted.copy(alpha = 0.3f),
                                        )
                                        Text(
                                            stringResource(R.string.scan_result_authenticity_subtitle),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TcgColors.CardInkMuted,
                                        )
                                    }
                                }
                                state.usedQuery?.let { query ->
                                    Text(
                                        stringResource(R.string.scan_query_used, query),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TcgColors.OnPlaymatMuted,
                                    )
                                }
                                Box(Modifier.padding(bottom = 24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun scanResultErrorMessage(error: ScanResultError): String = when (error) {
    ScanResultError.NO_TEXT -> stringResource(R.string.scan_error_no_text)
    ScanResultError.NO_MATCH -> stringResource(R.string.scan_error_no_match)
    ScanResultError.OFFLINE -> stringResource(R.string.scan_error_offline)
    ScanResultError.RECOGNITION_FAILED -> stringResource(R.string.scan_error_recognition)
    ScanResultError.MISSING_FILE -> stringResource(R.string.scan_result_missing_file)
}
