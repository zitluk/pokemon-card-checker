package com.pokemontcg.collection.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TcgChrome {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = TcgColors.FrameGoldBright,
        actionIconContentColor = TcgColors.FrameGoldBright,
    )

    @Composable
    fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
        selectedIconColor = TcgColors.FrameGoldBright,
        selectedTextColor = TcgColors.FrameGoldBright,
        indicatorColor = TcgColors.FrameGold.copy(alpha = 0.22f),
        unselectedIconColor = TcgColors.OnPlaymatMuted,
        unselectedTextColor = TcgColors.OnPlaymatMuted,
    )

    @Composable
    fun goldTextButtonColors() = ButtonDefaults.textButtonColors(
        contentColor = TcgColors.FrameGoldBright,
    )

    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = TcgColors.PokemonRed,
        contentColor = Color.White,
    )

    @Composable
    fun secondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = TcgColors.FrameGold,
        contentColor = TcgColors.CardInk,
    )
}
