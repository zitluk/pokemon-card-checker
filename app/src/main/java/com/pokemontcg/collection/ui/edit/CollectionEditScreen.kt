package com.pokemontcg.collection.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pokemontcg.collection.R
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.ui.components.labelCs
import com.pokemontcg.collection.ui.layout.ResponsiveContentColumn
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.layout.safeBottomInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionEditScreen(
    cardId: String? = null,
    itemId: Long? = null,
    viewModel: CollectionEditViewModel,
    contentPadding: PaddingValues,
    onDone: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var conditionMenu by remember { mutableStateOf(false) }
    val resolvedCardId = cardId ?: state.item?.cardId

    LaunchedEffect(cardId, itemId) {
        when {
            itemId != null -> viewModel.load(itemId)
            cardId != null -> viewModel.loadForCard(cardId)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        TopAppBar(title = { Text(stringResource(R.string.collection_entry_title)) })
        ResponsiveScreenScope { metrics ->
            ResponsiveContentColumn(metrics) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .safeBottomInsets()
                        .padding(vertical = metrics.verticalSpacing),
                ) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = viewModel::updateQuantity,
                        label = { Text(stringResource(R.string.field_quantity)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(
                        onClick = { conditionMenu = true },
                        modifier = Modifier.padding(vertical = metrics.verticalSpacing / 2),
                    ) {
                        Text(stringResource(R.string.field_condition, state.condition.labelCs()))
                    }
                    DropdownMenu(expanded = conditionMenu, onDismissRequest = { conditionMenu = false }) {
                        CardCondition.entries.forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition.labelCs()) },
                                onClick = {
                                    viewModel.updateCondition(condition)
                                    conditionMenu = false
                                },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.language,
                        onValueChange = viewModel::updateLanguage,
                        label = { Text(stringResource(R.string.field_language)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text(stringResource(R.string.field_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.purchasePrice,
                        onValueChange = viewModel::updatePurchasePrice,
                        label = { Text(stringResource(R.string.field_purchase_price)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            resolvedCardId?.let { viewModel.save(it) }
                        },
                        enabled = resolvedCardId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = metrics.verticalSpacing),
                    ) { Text(stringResource(R.string.action_save)) }
                }
            }
        }
    }
}
