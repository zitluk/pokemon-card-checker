package com.pokemontcg.collection.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
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
)

fun BoxWithConstraintsScope.rememberResponsiveMetrics(): ResponsiveMetrics {
    val widthClass = when {
        maxWidth < 600.dp -> WindowWidthClass.Compact
        maxWidth < 840.dp -> WindowWidthClass.Medium
        else -> WindowWidthClass.Expanded
    }
    val horizontalPadding = (maxWidth.value * 0.04f).dp.coerceIn(12.dp, 28.dp)
    val verticalSpacing = when (widthClass) {
        WindowWidthClass.Compact -> 12.dp
        WindowWidthClass.Medium -> 14.dp
        WindowWidthClass.Expanded -> 16.dp
    }
    val gridMinCellWidth = when (widthClass) {
        WindowWidthClass.Compact -> 280.dp
        WindowWidthClass.Medium -> 260.dp
        WindowWidthClass.Expanded -> 240.dp
    }
    return ResponsiveMetrics(
        widthClass = widthClass,
        maxWidth = maxWidth,
        horizontalPadding = horizontalPadding,
        verticalSpacing = verticalSpacing,
        gridMinCellWidth = gridMinCellWidth,
        detailMaxWidth = 600.dp,
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
    BoxWithConstraints(modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = metrics.horizontalPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            androidx.compose.foundation.layout.Column(
                Modifier
                    .widthIn(max = metrics.detailMaxWidth)
                    .fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

fun responsiveGridCells(metrics: ResponsiveMetrics): GridCells =
    GridCells.Adaptive(minSize = metrics.gridMinCellWidth)

fun responsiveContentPadding(metrics: ResponsiveMetrics): PaddingValues =
    PaddingValues(
        horizontal = metrics.horizontalPadding,
        vertical = metrics.horizontalPadding,
    )
