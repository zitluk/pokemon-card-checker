package com.pokemontcg.collection.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.pokemontcg.collection.R

private const val WATERMARK_ALPHA = 0.12f

@Composable
fun WatermarkedScreen(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.bg_pikachu_watermark),
            contentDescription = stringResource(R.string.watermark_cd),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            alpha = WATERMARK_ALPHA,
            contentScale = ContentScale.FillWidth,
        )
        Column(Modifier.fillMaxSize()) {
            header?.invoke()
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}
