package net.foxboi.mlem.export

import kotlinx.serialization.decodeFromString
import net.foxboi.mlem.Mlem
import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.serial.template.SerialTemplate

class TemplateCache(val assets: AssetManager) {
    private val cache = mutableMapOf<Asset, Template>()

    suspend fun load(asset: Asset): Template {
        return cache.getOrPut(asset) {
            val src = assets.text(asset)
            val serial = Mlem.yaml.decodeFromString<SerialTemplate>(src)
            serial.instantiate()
        }
    }
}