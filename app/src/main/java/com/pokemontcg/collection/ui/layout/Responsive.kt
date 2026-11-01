package com.pokemontcg.collection.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass {
    Compact,
    Medium,
    Expanded,
}

data class ResponsiveMetrics(
    val widthClass: WindowWidthClass,
    val maxWidth: Dp,
    val horizontalPadding: Dp,
    val verticalSpacing: Dp,
    val gridMinCellWidth: Dp,
    val detailMaxWidth: Dp,
    val searchResultsMaxHeight: Dp,
    val cardThumbnailHeight: Dp,
    val logoMaxHeight: Dp,
)

fun BoxWithConstraintsScope.rememberResponsiveMetrics(): ResponsiveMetrics {
    val widthClass = when {
        maxWidth < 600.dp -> WindowWidthClass.Compact
        maxWidth < 840.dp -> WindowWidthClass.Medium
        else -> WindowWidthClass.Expanded
    }
    val horizontalPadding = when (widthClass) {
        WindowWidthClass.Compact -> 16.dp
        WindowWidthClass.Medium -> 24.dp
        WindowWidthClass.Expanded -> 32.dp
    }
    val verticalSpacing = when (widthClass) {
        WindowWidthClass.Compact -> 12.dp
        WindowWidthClass.Medium -> 14.dp
        WindowWidthClass.Expanded -> 16.dp
    }
    val gridMinCellWidth = when (widthClass) {
        WindowWidthClass.Compact -> 168.dp
        WindowWidthClass.Medium -> 200.dp
        WindowWidthClass.Expanded -> 220.dp
    }
    val detailMaxWidth = when (widthClass) {
        WindowWidthClass.Compact -> maxWidth.coerceAtMost(560.dp)
        WindowWidthClass.Medium -> 720.dp
        WindowWidthClass.Expanded -> 900.dp
    }
    val searchResultsMaxHeight = when (widthClass) {
        WindowWidthClass.Compact -> 280.dp
        WindowWidthClass.Medium -> 340.dp
        WindowWidthClass.Expanded -> 420.dp
    }
    val cardThumbnailHeight = when (widthClass) {
        WindowWidthClass.Compact -> 220.dp
        WindowWidthClass.Medium -> 260.dp
        WindowWidthClass.Expanded -> 300.dp
    }
    val logoMaxHeight = when (widthClass) {
        WindowWidthClass.Compact -> 120.dp
        WindowWidthClass.Medium -> 140.dp
        WindowWidthClass.Expanded -> 160.dp
    }
    return ResponsiveMetrics(
        widthClass = widthClass,
        maxWidth = maxWidth,
        horizontalPadding = horizontalPadding,
        verticalSpacing = verticalSpacing,
        gridMinCellWidth = gridMinCellWidth,
        detailMaxWidth = detailMaxWidth,
        searchResultsMaxHeight = searchResultsMaxHeight,
        cardThumbnailHeight = cardThumbnailHeight,
        logoMaxHeight = logoMaxHeight,
    )
}

@Composable
fun ResponsiveScreenScope(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.(ResponsiveMetrics) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val metrics = rememberResponsiveMetrics()
        content(metrics)
    }
}

@Composable
fun ResponsiveContentColumn(
    metrics: ResponsiveMetrics,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        androidx.compose.foundation.layout.Column(
            Modifier
                .widthIn(max = metrics.detailMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = metrics.horizontalPadding),
        ) {
            content()
        }
    }
}

fun responsiveGridCells(metrics: ResponsiveMetrics): GridCells =
    GridCells.Adaptive(minSize = metrics.gridMinCellWidth)

fun responsiveContentPadding(metrics: ResponsiveMetrics): PaddingValues =
    PaddingValues(
        start = metrics.horizontalPadding,
        end = metrics.horizontalPadding,
        top = metrics.verticalSpacing,
        bottom = metrics.verticalSpacing + 72.dp,
    )

/** Edge-to-edge: status bar + cutout on full-bleed roots (tabs without outer top inset). */
fun Modifier.safeTopInsets(): Modifier =
    statusBarsPadding().displayCutoutPadding()

/** Bottom actions / bars above gesture nav when not inside Scaffold bottomBar slot. */
fun Modifier.safeBottomInsets(): Modifier = navigationBarsPadding()

@Composable
fun navigationBarInsetPadding(): PaddingValues =
    WindowInsets.navigationBars.asPaddingValues()
