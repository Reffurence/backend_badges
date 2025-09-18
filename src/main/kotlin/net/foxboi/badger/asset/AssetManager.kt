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
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray
import net.foxboi.badger.Badger
import net.foxboi.badger.EngineException
import net.foxboi.badger.Log
import java.lang.AutoCloseable
import java.nio.charset.Charset

class AssetManager(
    /**
     * The assets directory.
     */
    val assetDir: Path,

    /**
     * The temporary directory for storing cached downloads.
     */
    val tmpDir: Path = SystemTemporaryDirectory,

    /**
     * Time to live of cache entries, i.e., how long cache entries are considered valid. In milliseconds.
     */
    val cacheTtl: Long = 1000 * 60
) : AutoCloseable {
    private val client = HttpClient(CIO)

    private val urlCache = mutableMapOf<Url, CacheEntry>()
    private val uuidCache = mutableMapOf<String, CacheEntry>()

    private fun resolve(path: String): Path {
        val path = Path(assetDir, path)

        if (!SystemFileSystem.exists(path)) {
            throw EngineException("No such asset: $path")
        }

        return path
    }

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

    private fun entry(url: Url): CacheEntry {
        val existingEntry = urlCache[url]
        if (existingEntry != null) {
            return existingEntry
        }

        val uuid = uuid()
        val path = Path(tmpDir, uuid)
        val newEntry = CacheEntry(url, uuid, path, Badger.time())

        urlCache[url] = newEntry
        uuidCache[uuid] = newEntry
        return newEntry
    }

    private suspend fun download(url: Url): Path {
        val entry = entry(url)

        // Cache hit? Then just
        if (!entry.isOutdated(cacheTtl) && SystemFileSystem.exists(entry.path)) {
            return entry.path
        }

        Log.info { "Downloading $url" }

        val response = client.get(url)
        if (response.status != HttpStatusCode.OK) {
            throw DownloadException("Failed downloading $url: Received status code '${response.status}' but expected '${HttpStatusCode.OK}'")
        }

        entry.sink().use {
            // Write body to sink
            response.bodyAsChannel().copyAndClose(it.asByteWriteChannel())
        }

        return entry.path
    }

    suspend fun open(asset: Asset): Source {
        return when (asset) {
            is Asset.Fetch -> SystemFileSystem.source(download(asset.url)).buffered()
            is Asset.Local -> SystemFileSystem.source(resolve(asset.path)).buffered()
            is Asset.Data -> asset.url.copyToBuffer()
        }
    }

    suspend fun text(asset: Asset, charset: Charset = Charsets.UTF_8): String {
        return withContext(Dispatchers.IO) {
            open(asset).use { it.readText(charset) }
        }
    }

    suspend fun bytes(asset: Asset): ByteArray {
        return withContext(Dispatchers.IO) {
            open(asset).use { it.readByteArray() }
        }
    }


    fun clearCache() {
        urlCache.clear()
        uuidCache.clear()
    }

    override fun close() {
        client.close()
    }

    private class CacheEntry(val url: Url, val uuid: String, val path: Path, val timestamp: Long) {
        fun isOutdated(ttl: Long): Boolean {
            return Badger.time() > timestamp + ttl
        }

        fun sink(): Sink {
            val par = path.parent
            if (par != null) SystemFileSystem.createDirectories(par)

            return SystemFileSystem.sink(path).buffered()
        }
    }
}