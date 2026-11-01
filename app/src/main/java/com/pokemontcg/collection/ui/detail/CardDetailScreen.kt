package com.pokemontcg.collection.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.components.CardThumbnail
import com.pokemontcg.collection.ui.components.PriceTextOnCard
import com.pokemontcg.collection.ui.components.TcgCardBody
import com.pokemontcg.collection.ui.components.TcgCardTile
import com.pokemontcg.collection.ui.components.TcgCardTitle
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.layout.ResponsiveContentColumn
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.theme.TcgChrome
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    viewModel: CardDetailViewModel,
    contentPadding: PaddingValues,
    onEditCollection: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(cardId) { viewModel.load(cardId) }

    WatermarkedScreen(Modifier.fillMaxSize().padding(contentPadding)) {
        ResponsiveScreenScope { metrics ->
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(state.card?.name ?: stringResource(R.string.card_detail_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    },
                    colors = TcgChrome.topAppBarColors(),
                )
                when {
                    state.isLoading -> {
                        Text(
                            stringResource(R.string.loading),
                            modifier = Modifier.padding(metrics.horizontalPadding),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    state.error != null && state.card == null -> {
                        Text(
                            stringResource(R.string.error_generic),
                            modifier = Modifier.padding(metrics.horizontalPadding),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    else -> {
                        val card = state.card ?: return@Column
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            ResponsiveContentColumn(metrics) {
                                Column(
                                    Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(vertical = metrics.verticalSpacing),
                                    verticalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                                ) {
                                TcgCardTile {
                                    CardThumbnail(
                                        card.imageUrl,
                                        card.name,
                                        height = metrics.cardThumbnailHeight,
                                    )
                                    TcgCardTitle(card.name)
                                    TcgCardBody(
                                        card.setName ?: stringResource(R.string.collection_unknown_set),
                                        muted = true,
                                    )
                                    card.rarity?.let { TcgCardBody(it) }
                                    PriceTextOnCard(card.priceEur, card.priceUpdatedAt, card.isPriceStale)
                                }
                                Button(
                                    onClick = { viewModel.refreshPrice(cardId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TcgChrome.secondaryButtonColors(),
                                ) { Text(stringResource(R.string.refresh_price)) }
                                state.collectionItem?.let { item ->
                                    Text(
                                        stringResource(R.string.in_collection_qty, item.quantity),
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                                Button(
                                    onClick = { scope.launch { viewModel.quickAdd(cardId) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TcgChrome.primaryButtonColors(),
                                ) {
                                    Text(
                                        if (state.collectionItem == null) {
                                            stringResource(R.string.add_to_collection)
                                        } else {
                                            stringResource(R.string.quick_save_qty_one)
                                        },
                                    )
                                }
                                Button(
                                    onClick = onEditCollection,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TcgChrome.secondaryButtonColors(),
                                ) {
                                    Text(stringResource(R.string.edit_in_collection))
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
