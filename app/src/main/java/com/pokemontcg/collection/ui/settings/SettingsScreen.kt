package com.pokemontcg.collection.ui.settings

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.theme.TcgChrome
import com.pokemontcg.collection.ui.theme.TcgColors
import androidx.compose.foundation.layout.fillMaxSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues,
) {
    val darkTheme by viewModel.darkTheme.collectAsState()

    WatermarkedScreen(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        header = {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val pad = (maxWidth.value * 0.04f).dp.coerceIn(12.dp, 28.dp)
                PokemonBrandHeader(
                    screenTitle = stringResource(R.string.nav_settings),
                    horizontalPadding = pad,
                )
            }
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.settings_currency),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(Modifier.padding(vertical = 12.dp)) {
                Text(
                    stringResource(R.string.settings_dark_theme),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Switch(checked = darkTheme, onCheckedChange = viewModel::setDarkTheme)
            }
            Button(
                onClick = viewModel::clearCaches,
                modifier = Modifier.fillMaxWidth(),
                colors = TcgChrome.secondaryButtonColors(),
            ) {
                Text(stringResource(R.string.settings_clear_cache))
            }
            Text(
                stringResource(R.string.disclaimer_prices),
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.disclaimer_authenticity),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.disclaimer_fan),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.attribution_logo_commons),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TcgColors.OnPlaymatMuted,
            )
            Text(
                stringResource(R.string.attribution_tcgdex),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TcgColors.OnPlaymatMuted,
            )
            }
        }
    }
}
