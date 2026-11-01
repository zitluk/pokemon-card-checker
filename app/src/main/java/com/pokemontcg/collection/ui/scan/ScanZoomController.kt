package com.pokemontcg.collection.ui.scan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

class ScanZoomController {
    private var _linearZoom by mutableFloatStateOf(0f)
    val linearZoom: Float get() = _linearZoom
    var maxZoomRatio by mutableFloatStateOf(1f)
    var minZoomRatio by mutableFloatStateOf(1f)

    internal var applyLinearZoom: ((Float) -> Unit)? = null

    fun setLinearZoom(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _linearZoom = clamped
        applyLinearZoom?.invoke(clamped)
    }

    fun resetZoom() {
        setLinearZoom(0f)
    }

    val displayRatio: Float
        get() = minZoomRatio + (maxZoomRatio - minZoomRatio) * linearZoom
}
