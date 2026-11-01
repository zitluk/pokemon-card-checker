package com.pokemontcg.collection.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pokemontcg.collection.R
import com.pokemontcg.collection.data.model.CardCondition
import com.pokemontcg.collection.ui.collection.CollectionSort

@Composable
fun CardCondition.labelCs(): String = when (this) {
    CardCondition.NM -> stringResource(R.string.condition_nm)
    CardCondition.LP -> stringResource(R.string.condition_lp)
    CardCondition.MP -> stringResource(R.string.condition_mp)
    CardCondition.HP -> stringResource(R.string.condition_hp)
    CardCondition.DMG -> stringResource(R.string.condition_dmg)
}

@Composable
fun CollectionSort.labelCs(): String = when (this) {
    CollectionSort.DATE_ADDED -> stringResource(R.string.sort_date_added)
    CollectionSort.NAME -> stringResource(R.string.sort_name)
    CollectionSort.SET -> stringResource(R.string.sort_set)
    CollectionSort.VALUE -> stringResource(R.string.sort_value)
}
