package com.pokemontcg.collection.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pokemontcg.collection.ui.theme.TcgColors

private val CardShape = RoundedCornerShape(12.dp)
private val GoldBorder = BorderStroke(2.5.dp, TcgColors.FrameGold)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcgCardTile(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = modifier.fillMaxWidth()
    val colors = CardDefaults.cardColors(
        containerColor = TcgColors.CardFaceCream,
        contentColor = TcgColors.CardInk,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)

    CompositionLocalProvider(LocalContentColor provides TcgColors.CardInk) {
        if (onClick != null) {
            Card(
                onClick = onClick,
                modifier = cardModifier,
                shape = CardShape,
                colors = colors,
                elevation = elevation,
                border = GoldBorder,
            ) {
                Column(Modifier.padding(12.dp), content = content)
            }
        } else {
            Card(
                modifier = cardModifier,
                shape = CardShape,
                colors = colors,
                elevation = elevation,
                border = GoldBorder,
            ) {
                Column(Modifier.padding(12.dp), content = content)
            }
        }
    }
}

@Composable
fun TcgCardTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(
            color = TcgColors.CardInk,
            fontWeight = FontWeight.SemiBold,
        ),
        maxLines = 1,
    )
}

@Composable
fun TcgCardBody(text: String, modifier: Modifier = Modifier, muted: Boolean = false) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (muted) TcgColors.CardInkMuted else TcgColors.CardInk,
        ),
    )
}

@Composable
fun PriceTextOnCard(eur: Double?, updatedAt: Long?, stale: Boolean) {
    CompositionLocalProvider(LocalContentColor provides TcgColors.CardInk) {
        PriceText(eur, updatedAt, stale)
    }
}
