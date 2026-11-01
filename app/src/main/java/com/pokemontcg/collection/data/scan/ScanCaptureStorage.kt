package com.pokemontcg.collection.data.scan

import java.io.File

object ScanCaptureStorage {
    private const val PREFIX = "scan_capture_"

    fun newCaptureFile(cacheDir: File): File =
        File(cacheDir, "$PREFIX${System.currentTimeMillis()}.jpg")

    fun deleteIfExists(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
