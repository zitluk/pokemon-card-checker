package com.pokemontcg.collection.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.ui.unit.dp
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.components.TcgCardBody
import com.pokemontcg.collection.ui.components.TcgCardTile
import com.pokemontcg.collection.ui.components.TcgCardTitle
import com.pokemontcg.collection.ui.layout.ResponsiveMetrics
import com.pokemontcg.collection.ui.theme.TcgColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSearchPanel(
    viewModel: SearchViewModel,
    metrics: ResponsiveMetrics,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val recent by viewModel.recentSearches.collectAsState()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TcgColors.FrameGoldBright,
        unfocusedBorderColor = TcgColors.FrameGold.copy(alpha = 0.5f),
        focusedLabelColor = TcgColors.FrameGoldBright,
        cursorColor = TcgColors.FrameGoldBright,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    )
    val showResults = state.query.trim().length >= 2

    Column(modifier) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalSpacing / 2),
            label = { Text(stringResource(R.string.collection_search_hint)) },
            placeholder = { Text(stringResource(R.string.collection_search_hint)) },
            singleLine = true,
            colors = fieldColors,
        )
        if (state.isLoading) {
            CircularProgressIndicator(
                Modifier.padding(horizontal = metrics.horizontalPadding),
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
        if (!showResults && recent.isNotEmpty()) {
            Text(
                stringResource(R.string.search_recent),
                modifier = Modifier.padding(horizontal = metrics.horizontalPadding),
                style = MaterialTheme.typography.titleSmall,
                color = TcgColors.FrameGoldBright,
            )
            recent.take(5).forEach { entry ->
                Text(
                    entry.query,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onQueryChange(entry.query) }
                        .padding(
                            horizontal = metrics.horizontalPadding,
                            vertical = metrics.verticalSpacing / 2,
                        ),
                )
            }
        }
        if (showResults && state.results.isNotEmpty()) {
            Text(
                stringResource(R.string.collection_search_results),
                modifier = Modifier.padding(horizontal = metrics.horizontalPadding),
                style = MaterialTheme.typography.titleSmall,
                color = TcgColors.FrameGoldBright,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .padding(horizontal = metrics.horizontalPadding),
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
