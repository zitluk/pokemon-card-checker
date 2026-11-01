package com.pokemontcg.collection.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.theme.TcgColors

/** International Pokémon logo aspect (Commons SVG ~269×99). */
private const val LOGO_ASPECT_RATIO = 269f / 99f

@Composable
fun PokemonBrandHeader(
    screenTitle: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // Width drives size: span full content width (minus 8dp side inset on the image).
            val logoWidth = maxWidth - 16.dp
            val logoHeight = logoWidth / LOGO_ASPECT_RATIO

            Image(
                painter = painterResource(R.drawable.logo_pokemon_official),
                contentDescription = stringResource(R.string.logo_cd),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(logoHeight),
                contentScale = ContentScale.FillWidth,
            )
        }
        Text(
            text = screenTitle,
            style = MaterialTheme.typography.titleMedium,
            color = TcgColors.FrameGoldBright,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
