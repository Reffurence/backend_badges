package net.foxboi.badger.asset

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import net.foxboi.badger.Badger
import net.foxboi.badger.EngineException
import net.foxboi.badger.Log
import java.lang.AutoCloseable
import java.nio.charset.Charset

/**
 * The [AssetManager] manages the loading and caching of assets.
 *
 * @param assetDir The assets directory, where local assets are loaded from.
 * @param tmpDir The assets directory, where local assets are loaded from.
 * @param cacheTtl Time to live of cache entries, i.e., how long cache entries are considered valid. In milliseconds.
 */
class AssetManager(
    val assetDir: Path,
    val tmpDir: Path,
    val cacheTtl: Long = 1000 * 60
) : AutoCloseable {
    private val client = HttpClient(CIO)

    private val urlCache = mutableMapOf<Url, CacheEntry>()
    private val uuidCache = mutableMapOf<String, CacheEntry>()

    /**
     * Resolves a local asset as [Path].
     */
    private fun resolve(path: String): Path {
        val path = Path(assetDir, path)

        if (!SystemFileSystem.exists(path)) {
            throw EngineException("No such asset: $path")
        }

        return path
    }

    /**
     * Generates a new download UUID, and ensures even the tiniest chance of clashing with another is accounted for.
     */
    private fun uuid(): String {
        var uuid = Badger.uuid()

        // Extremely likely that we don't hit a cache entry by random chance but
        // try 5 times more if we do
        var i = 5
        while (uuid in uuidCache && i > 0) {
            uuid = Badger.uuid()
            i--
        }

        // By this point the chance that we still hit a cache entry is astronomically close to 0 but
        // in case we still do somehow, let's just clear the cache entry
        val oldEntry = uuidCache[uuid]
        if (oldEntry != null) {
            urlCache.remove(oldEntry.url)
            uuidCache.remove(oldEntry.uuid)
        }

        return uuid
    }

    /**
     * Returns the [CacheEntry] matching the given [Url], or creates one.
     */
    private fun entry(url: Url): CacheEntry {
        val existingEntry = urlCache[url]
        if (existingEntry != null) {
            return existingEntry
        }

        val uuid = uuid()
        val path = Path(tmpDir, uuid)
        val newEntry = CacheEntry(url, uuid, path, Badger.time(), cacheTtl)

        urlCache[url] = newEntry
        uuidCache[uuid] = newEntry
        return newEntry
    }

    /**
     * Downloads an asset from [Url] and returns the [Path] to the download.
     */
    private suspend fun download(url: Url): Path {
        val entry = entry(url)

        if (!entry.isOutdated() && SystemFileSystem.exists(entry.path)) {
            return entry.path
        }

        Log.info { "Downloading $url" }

        val response = client.get(url)
        if (response.status != HttpStatusCode.OK) {
            throw DownloadException("Failed downloading $url: Received status code '${response.status}' but expected '${HttpStatusCode.OK}'")
        }

        val age = (response.headers["Age"]?.toLongOrNull() ?: 0L) * 1000L
        val ttl = computeTtl(cacheTtl, age, response.cacheControl())
        entry.ttl = ttl

        entry.sink().use {
            // Write body to sink
            response.bodyAsChannel().copyAndClose(it.asByteWriteChannel())
        }

        return entry.path
    }

    /**
     * Opens an [Asset] as a [Source]. May suspend to download the asset.
     */
    suspend fun open(asset: Asset): Source {
        return when (asset) {
            is Asset.Remote -> SystemFileSystem.source(download(asset.url)).buffered()
            is Asset.Local -> SystemFileSystem.source(resolve(asset.path)).buffered()
            is Asset.Data -> asset.uri.toBuffer()
        }
    }

    /**
     * Reads an [Asset] as a [String] with given charset. May suspend to download the asset.
     */
    suspend fun text(asset: Asset, charset: Charset = Charsets.UTF_8): String {
        if (asset is Asset.Data) {
            // Shortcut, it's faster than writing bytes to a buffer and reading them back into a string
            // asynchronously

            // Also, the URI may specify its own charset, which we want to respect first. This is not respected by
            // open(...).
            return asset.uri.toContentString(fallbackCharset = charset)
        }

        return withContext(Dispatchers.IO) {
            open(asset).use { it.readText(charset) }
        }
    }

    /**
     * Reads an [Asset] as a [ByteArray]. May suspend to download the asset.
     */
    suspend fun bytes(asset: Asset): ByteArray {
        if (asset is Asset.Data) {
            // Shortcut, same reason
            return asset.uri.toByteArray()
        }

        return withContext(Dispatchers.IO) {
            open(asset).use { it.readByteArray() }
        }
    }


    /**
     * Clears the entire download cache. This does not remove the cached files, those must be removed manually.
     * Cache is always checked against the file system so one may clear the cache simply by removing the cached files.
     */
    fun clearCache() {
        urlCache.clear()
        uuidCache.clear()
    }

    /**
     * Closes the HTTP client.
     */
    override fun close() {
        client.close()
    }

    private class CacheEntry(val url: Url, val uuid: String, val path: Path, val timestamp: Long, var ttl: Long) {
        fun isOutdated(): Boolean {
            return Badger.time() > timestamp + ttl
        }

        fun sink(): Sink {
            val par = path.parent
            if (par != null) SystemFileSystem.createDirectories(par)

            return SystemFileSystem.sink(path).buffered()
        }
    }
}