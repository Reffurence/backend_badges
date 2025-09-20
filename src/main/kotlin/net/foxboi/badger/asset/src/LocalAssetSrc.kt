package net.foxboi.badger.asset.src

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class LocalAssetSrc(
    val baseDir: Path
) : AssetSrc {
    override fun exists(path: String): Boolean {
        return SystemFileSystem.exists(Path(baseDir, path))
    }

    override fun read(path: String): Source {
        return SystemFileSystem.source(Path(baseDir, path)).buffered()
    }

    override fun close() {
        // Can't close a local file system
    }
}