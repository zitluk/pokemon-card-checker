package com.pokemontcg.collection.data.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@JsonClass(generateAdapter = true)
data class GitHubLatestReleaseDto(
    @Json(name = "tag_name") val tagName: String,
    @Json(name = "html_url") val htmlUrl: String,
)

sealed class UpdateCheckOutcome {
    data object UpToDate : UpdateCheckOutcome()
    data class UpdateAvailable(
        val remoteVersionLabel: String,
        val releaseUrl: String,
    ) : UpdateCheckOutcome()
}

class UpdateChecker(
    private val okHttp: OkHttpClient,
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(GitHubLatestReleaseDto::class.java)

    suspend fun checkLatestRelease(currentVersionName: String): Result<UpdateCheckOutcome> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(LATEST_RELEASE_URL)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", USER_AGENT)
                    .get()
                    .build()
                okHttp.newCall(request).execute().use { response ->
                    if (response.code == 404) {
                        return@withContext Result.failure(NoReleaseException())
                    }
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IOException("HTTP ${response.code}"))
                    }
                    val body = response.body?.string()
                        ?: return@withContext Result.failure(IOException("Empty response"))
                    val release = adapter.fromJson(body)
                        ?: return@withContext Result.failure(IOException("Parse failed"))
                    val remote = VersionParts.parse(release.tagName)
                    val local = VersionParts.parse(currentVersionName)
                    if (VersionParts.isNewer(remote, local)) {
                        Result.success(
                            UpdateCheckOutcome.UpdateAvailable(
                                remoteVersionLabel = release.tagName.removePrefix("v").removePrefix("V"),
                                releaseUrl = release.htmlUrl,
                            ),
                        )
                    } else {
                        Result.success(UpdateCheckOutcome.UpToDate)
                    }
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }

    class NoReleaseException : IOException("No releases published")

    companion object {
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/zitluk/pokemon-card-checker/releases/latest"
        private const val USER_AGENT = "PokemonCardChecker/1.0 (Android)"
    }
}

internal object VersionParts {
    fun parse(raw: String): List<Int> =
        raw.trim()
            .removePrefix("v")
            .removePrefix("V")
            .split('.', '-', '_')
            .mapNotNull { part -> part.takeWhile { it.isDigit() }.toIntOrNull() }
            .ifEmpty { listOf(0) }

    fun isNewer(remote: List<Int>, local: List<Int>): Boolean {
        val length = maxOf(remote.size, local.size)
        for (i in 0 until length) {
            val r = remote.getOrElse(i) { 0 }
            val l = local.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
