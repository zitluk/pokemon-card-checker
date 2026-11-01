package com.pokemontcg.collection.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TcgDarkScheme = darkColorScheme(
    primary = TcgColors.PokemonRed,
    onPrimary = Color.White,
    primaryContainer = TcgColors.PokemonRedDark,
    onPrimaryContainer = Color.White,
    secondary = TcgColors.FrameGold,
    onSecondary = TcgColors.CardInk,
    secondaryContainer = TcgColors.GoldAccent,
    onSecondaryContainer = TcgColors.CardInk,
    tertiary = TcgColors.FrameGoldBright,
    onTertiary = TcgColors.CardInk,
    background = TcgColors.PlaymatDark,
    onBackground = TcgColors.OnPlaymat,
    surface = TcgColors.PlaymatSurface,
    onSurface = TcgColors.OnPlaymat,
    surfaceVariant = TcgColors.PlaymatElevated,
    onSurfaceVariant = TcgColors.OnPlaymatMuted,
    outline = TcgColors.FrameGold.copy(alpha = 0.55f),
    error = Color(0xFFCF6679),
    onError = Color.Black,
)

/** Lighter playmat — card tiles stay cream + gold frame */
private val TcgLightScheme = lightColorScheme(
    primary = TcgColors.PokemonRed,
    onPrimary = Color.White,
    secondary = TcgColors.GoldAccent,
    onSecondary = TcgColors.CardInk,
    background = Color(0xFF232848),
    onBackground = TcgColors.OnPlaymat,
    surface = Color(0xFF2A3154),
    onSurface = TcgColors.OnPlaymat,
    surfaceVariant = TcgColors.PlaymatElevated,
    onSurfaceVariant = TcgColors.OnPlaymatMuted,
    outline = TcgColors.FrameGold.copy(alpha = 0.65f),
)

@Composable
fun PokemonCardCheckerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) TcgDarkScheme else TcgLightScheme,
        content = content,
    )
}
