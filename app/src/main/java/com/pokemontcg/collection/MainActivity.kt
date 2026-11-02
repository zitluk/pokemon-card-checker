package com.pokemontcg.collection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokemontcg.collection.ui.AppViewModelFactory
import com.pokemontcg.collection.ui.navigation.PokemonAppNav
import com.pokemontcg.collection.ui.settings.SettingsViewModel
import com.pokemontcg.collection.ui.theme.PokemonCardCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // decorFitsSystemWindows=true keeps IME resize/show reliable (edge-to-edge breaks IME on some API 35 devices).
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val container = (application as PokemonApplication).container
        val factory = AppViewModelFactory(container, application)
        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = factory)
            val darkTheme by settingsVm.darkTheme.collectAsState()
            PokemonCardCheckerTheme(darkTheme = darkTheme) {
                PokemonAppNav(factory)
            }
        }
    }
}
