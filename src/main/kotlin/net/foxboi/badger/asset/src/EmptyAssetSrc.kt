package net.foxboi.badger.asset.src

import kotlinx.io.Source

object EmptyAssetSrc : AssetSrc {
    override fun exists(path: String): Boolean {
        return false
    }

    override fun read(path: String): Source {
        throw AssetSrcException("Empty asset source: $path does not exist")
    }

    override fun close() {
    }
}