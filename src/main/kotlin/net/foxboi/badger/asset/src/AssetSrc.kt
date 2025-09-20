package net.foxboi.badger.asset.src

import kotlinx.io.Source
import java.lang.AutoCloseable

/**
 * A (potentially remote) file system. Operations on this are not suspended but might block, especially when fetching
 * files from remote file systems.
 */
interface AssetSrc : AutoCloseable {
    fun exists(path: String): Boolean
    fun read(path: String): Source
    override fun close()
}