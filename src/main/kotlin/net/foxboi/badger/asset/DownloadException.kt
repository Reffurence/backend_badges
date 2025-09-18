package net.foxboi.badger.asset

import net.foxboi.badger.EngineException

/**
 * Exception thrown by [AssetManager] when a download fails.
 */
class DownloadException(message: String?) : EngineException(message)