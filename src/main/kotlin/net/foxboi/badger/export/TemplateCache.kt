package net.foxboi.badger.export

import kotlinx.serialization.decodeFromString
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.Asset
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Template
import net.foxboi.badger.serial.template.SerialTemplate

/**
 * Caches loaded [Template]s during a batch export.
 */
class TemplateCache(val assets: AssetManager) {
    private val cache = mutableMapOf<Asset, Template>()

    suspend fun load(asset: Asset): Template {
        return cache.getOrPut(asset) {
            val src = assets.text(asset)
            val serial = Badger.yaml.decodeFromString<SerialTemplate>(src)
            serial.instantiate()
        }
    }
}