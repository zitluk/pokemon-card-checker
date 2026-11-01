package com.pokemontcg.collection.ui.scan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScanZoomController {
    private var _linearZoom by mutableFloatStateOf(0f)
    val linearZoom: Float get() = _linearZoom
    var maxZoomRatio by mutableFloatStateOf(1f)
    var minZoomRatio by mutableFloatStateOf(1f)

    private var _torchEnabled by mutableStateOf(false)
    val torchEnabled: Boolean get() = _torchEnabled
    var hasFlashUnit by mutableStateOf(false)
    var cameraBound by mutableStateOf(false)

    internal var applyLinearZoom: ((Float) -> Unit)? = null
    internal var applyTorch: ((Boolean) -> Unit)? = null

    fun setLinearZoom(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _linearZoom = clamped
        applyLinearZoom?.invoke(clamped)
    }

    fun resetZoom() {
        setLinearZoom(0f)
    }

    fun setTorch(enabled: Boolean) {
        if (enabled && !hasFlashUnit) return
        _torchEnabled = enabled
        applyTorch?.invoke(enabled)
    }

    fun toggleTorch(): Boolean {
        setTorch(!torchEnabled)
        return torchEnabled
    }

    fun releaseCameraSession() {
        if (_torchEnabled) {
            applyTorch?.invoke(false)
        }
        _torchEnabled = false
        applyLinearZoom = null
        applyTorch = null
        hasFlashUnit = false
        cameraBound = false
    }

    val displayRatio: Float
        get() = minZoomRatio + (maxZoomRatio - minZoomRatio) * linearZoom
}
