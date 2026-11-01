package com.pokemontcg.collection.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.components.PokemonBrandHeader
import com.pokemontcg.collection.ui.components.TcgCardBody
import com.pokemontcg.collection.ui.components.TcgCardTile
import com.pokemontcg.collection.ui.components.TcgCardTitle
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.layout.rememberResponsiveMetrics
import com.pokemontcg.collection.ui.layout.responsiveContentPadding
import com.pokemontcg.collection.ui.layout.responsiveGridCells
import com.pokemontcg.collection.ui.theme.TcgColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    contentPadding: PaddingValues,
    onCardClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TcgColors.FrameGoldBright,
        unfocusedBorderColor = TcgColors.FrameGold.copy(alpha = 0.5f),
        focusedLabelColor = TcgColors.FrameGoldBright,
        cursorColor = TcgColors.FrameGoldBright,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    )

    WatermarkedScreen(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        header = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val pad = (maxWidth.value * 0.04f).dp.coerceIn(12.dp, 28.dp)
                PokemonBrandHeader(
                    screenTitle = stringResource(R.string.search_title),
                    horizontalPadding = pad,
                )
            }
        },
    ) {
        ResponsiveScreenScope { metrics ->
            Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalSpacing),
                    label = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    colors = fieldColors,
                )
                if (state.isLoading) {
                    CircularProgressIndicator(
                        Modifier.padding(metrics.horizontalPadding),
                        color = TcgColors.FrameGoldBright,
                    )
                }
                if (state.error != null) {
                    Text(
                        stringResource(R.string.search_failed),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = metrics.horizontalPadding),
                    )
                }
                LazyVerticalGrid(
                    columns = responsiveGridCells(metrics),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = responsiveContentPadding(metrics),
                    horizontalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                    verticalArrangement = Arrangement.spacedBy(metrics.verticalSpacing),
                ) {
                    items(state.results, key = { it.id }) { card ->
                        TcgCardTile(onClick = { onCardClick(card.id) }) {
                            TcgCardTitle(card.name)
                            TcgCardBody(card.id, muted = true)
                        }
                    }
                }
            }
        }
    }
}
