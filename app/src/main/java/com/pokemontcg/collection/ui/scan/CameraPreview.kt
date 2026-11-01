package com.pokemontcg.collection.ui.scan

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val TAG = "ScanCamera"

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner, imageCapture) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            try {
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
            }
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}
