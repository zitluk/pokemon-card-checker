package com.pokemontcg.collection.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pokemontcg.collection.R
import com.pokemontcg.collection.data.scan.ScanCaptureStorage
import com.pokemontcg.collection.ui.components.PokemonBrandHeader
import com.pokemontcg.collection.ui.components.WatermarkedScreen
import com.pokemontcg.collection.ui.layout.ResponsiveScreenScope
import com.pokemontcg.collection.ui.layout.rememberResponsiveMetrics
import com.pokemontcg.collection.ui.theme.TcgChrome
import com.pokemontcg.collection.ui.theme.TcgColors
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    contentPadding: PaddingValues,
    onNavigateToResult: (capturePath: String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.setCameraPermission(granted) }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermission(granted)
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.pendingCapturePath) {
        val path = state.pendingCapturePath ?: return@LaunchedEffect
        viewModel.consumePendingCapturePath()
        onNavigateToResult(path)
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TcgColors.PlaymatSurface.copy(alpha = 0f),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TcgChrome.topAppBarColors(),
            )
        },
    ) { snackPadding ->
        WatermarkedScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(snackPadding),
            header = {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val pad = (maxWidth.value * 0.04f).dp.coerceIn(12.dp, 28.dp)
                    PokemonBrandHeader(
                        screenTitle = stringResource(R.string.scan_title),
                        horizontalPadding = pad,
                    )
                }
            },
        ) {
            ResponsiveScreenScope {
                val metrics = rememberResponsiveMetrics()
                Column(Modifier.fillMaxSize()) {
                    Text(
                        stringResource(R.string.scan_disclaimer),
                        modifier = Modifier.padding(horizontal = metrics.horizontalPadding),
                        style = MaterialTheme.typography.bodySmall,
                        color = TcgColors.OnPlaymatMuted,
                    )

                    when {
                        !state.hasCameraPermission -> {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(metrics.horizontalPadding),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.scan_error_no_permission),
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = TcgChrome.primaryButtonColors(),
                                ) { Text(stringResource(R.string.scan_grant_permission)) }
                            }
                        }
                        else -> {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(metrics.horizontalPadding)
                                    .aspectRatio(3f / 4f),
                            ) {
                                CameraPreview(
                                    modifier = Modifier.fillMaxSize(),
                                    imageCapture = imageCapture,
                                )
                            }
                            Button(
                                onClick = {
                                    captureToFile(
                                        imageCapture = imageCapture,
                                        context = context,
                                        executor = ContextCompat.getMainExecutor(context),
                                        viewModel = viewModel,
                                    )
                                },
                                enabled = !state.isCapturing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = metrics.horizontalPadding),
                                colors = TcgChrome.primaryButtonColors(),
                            ) { Text(stringResource(R.string.scan_capture)) }

                            if (state.isCapturing) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(metrics.horizontalPadding),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = TcgColors.FrameGoldBright)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun captureToFile(
    imageCapture: ImageCapture,
    context: android.content.Context,
    executor: Executor,
    viewModel: ScanViewModel,
) {
    viewModel.onCaptureStarted()
    val file = ScanCaptureStorage.newCaptureFile(context.cacheDir)
    val output = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        output,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                viewModel.onPhotoSaved(file.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                ScanCaptureStorage.deleteIfExists(file.absolutePath)
                viewModel.onCaptureFailed()
            }
        },
    )
}
