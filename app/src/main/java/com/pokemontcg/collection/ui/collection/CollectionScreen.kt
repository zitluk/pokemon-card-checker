package com.pokemontcg.collection.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pokemontcg.collection.R
import com.pokemontcg.collection.data.model.CollectionItem
import com.pokemontcg.collection.data.model.CollectionStats
import com.pokemontcg.collection.ui.components.PriceTextOnCard
import com.pokemontcg.collection.ui.components.TcgCardBody
import com.pokemontcg.collection.ui.components.TcgCardTile
import com.pokemontcg.collection.ui.components.TcgCardTitle
import com.pokemontcg.collection.ui.components.PokemonBrandHeader
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.components.labelCs
import com.pokemontcg.collection.ui.layout.ResponsiveMetrics
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.layout.rememberResponsiveMetrics
import com.pokemontcg.collection.ui.layout.responsiveContentPadding
import com.pokemontcg.collection.ui.layout.responsiveGridCells
import com.pokemontcg.collection.ui.search.CatalogSearchPanel
import com.pokemontcg.collection.ui.search.SearchViewModel
import com.pokemontcg.collection.ui.theme.TcgChrome
import com.pokemontcg.collection.ui.theme.TcgColors
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel,
    searchViewModel: SearchViewModel,
    contentPadding: PaddingValues,
    onOpenItem: (CollectionItem) -> Unit,
    onEditItem: (CollectionItem) -> Unit,
    onStartScan: () -> Unit,
    onSearchCardClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.priceRefreshCount) {
        state.priceRefreshCount?.let { count ->
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.price_refresh_message, count),
            )
            viewModel.consumePriceRefreshMessage()
        }
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        containerColor = TcgColors.PlaymatSurface.copy(alpha = 0f),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartScan,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 8.dp),
                containerColor = TcgColors.FrameGoldBright,
                contentColor = TcgColors.PlaymatSurface,
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = stringResource(R.string.collection_scan_fab),
                )
            }
        },
    ) { innerPadding ->
    WatermarkedScreen(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        header = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val headerMetrics = rememberResponsiveMetrics()
                PokemonBrandHeader(
                    screenTitle = stringResource(R.string.collection_title),
                    horizontalPadding = headerMetrics.horizontalPadding,
                    logoMaxHeight = headerMetrics.logoMaxHeight,
                )
            }
        },
    ) {
        ResponsiveScreenScope { metrics ->
            Column(Modifier.fillMaxSize().imePadding()) {
                StatsHeader(state.stats, metrics)
                CatalogSearchPanel(
                    viewModel = searchViewModel,
                    metrics = metrics,
                    onCardClick = onSearchCardClick,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = metrics.horizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = { sortMenuExpanded = true },
                        colors = TcgChrome.goldTextButtonColors(),
                    ) {
                        Text(stringResource(R.string.collection_sort_label, state.sort.labelCs()))
                    }
                    DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        CollectionSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.labelCs()) },
                                onClick = {
                                    viewModel.setSort(sort)
                                    sortMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                PullToRefreshBox(
                    isRefreshing = state.isRefreshingPrices,
                    onRefresh = { viewModel.refreshPrices() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.items.isEmpty()) {
                        Text(
                            stringResource(R.string.collection_empty),
                            modifier = Modifier.padding(metrics.horizontalPadding),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = responsiveGridCells(metrics),
                            contentPadding = responsiveContentPadding(metrics),
                            horizontalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                            verticalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                CollectionItemCard(item, onOpenItem, onEditItem)
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun CollectionItemCard(
    item: CollectionItem,
    onOpenItem: (CollectionItem) -> Unit,
    onEditItem: (CollectionItem) -> Unit,
) {
    TcgCardTile(onClick = { onOpenItem(item) }) {
        TcgCardTitle(item.cardName)
        TcgCardBody(
            item.setName ?: stringResource(R.string.collection_unknown_set),
            muted = true,
        )
        TcgCardBody(
            stringResource(
                R.string.collection_qty_condition,
                item.quantity,
                item.condition.labelCs(),
            ),
        )
        PriceTextOnCard(
            item.marketPriceEur,
            item.priceUpdatedAt,
            stale = item.priceUpdatedAt == null ||
                (System.currentTimeMillis() - (item.priceUpdatedAt ?: 0) > 86_400_000),
        )
        TextButton(
            onClick = { onEditItem(item) },
            colors = ButtonDefaults.textButtonColors(contentColor = TcgColors.PokemonRed),
        ) {
            Text(stringResource(R.string.collection_edit_entry))
        }
    }
}

@Composable
private fun StatsHeader(stats: CollectionStats, metrics: ResponsiveMetrics) {
    val currency = NumberFormat.getCurrencyInstance(Locale("cs", "CZ"))
    Column(Modifier.padding(metrics.horizontalPadding)) {
        Text(
            stringResource(R.string.collection_stats_cards, stats.totalCards, stats.distinctEntries),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.collection_stats_value, currency.format(stats.estimatedValueEur)),
            style = MaterialTheme.typography.bodyLarge,
            color = TcgColors.FrameGoldBright,
        )
    }
}
