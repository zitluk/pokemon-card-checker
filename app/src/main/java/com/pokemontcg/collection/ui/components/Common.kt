package com.pokemontcg.collection.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pokemontcg.collection.ui.theme.TcgColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pokemontcg.collection.R
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CardThumbnail(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun PriceText(eur: Double?, updatedAt: Long?, stale: Boolean) {
    val dash = stringResource(R.string.price_dash)
    val formatted = eur?.let { eurFormat.format(it) } ?: dash
    val suffix = when {
        eur == null -> stringResource(R.string.price_no_data)
        stale -> stringResource(R.string.price_cached_stale)
        else -> ""
    }
    val ink = LocalContentColor.current
    val muted = if (ink == TcgColors.CardInk) TcgColors.CardInkMuted else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = stringResource(R.string.price_est_eur, formatted + suffix),
        style = MaterialTheme.typography.titleMedium.copy(color = ink),
    )
    updatedAt?.let {
        Text(
            text = stringResource(R.string.price_updated, dateFormat.format(Instant.ofEpochMilli(it))),
            style = MaterialTheme.typography.bodySmall.copy(color = muted),
        )
    }
}

@Composable
fun OneLineTitle(text: String) {
    Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

private val eurFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("cs", "CZ"))
private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())
