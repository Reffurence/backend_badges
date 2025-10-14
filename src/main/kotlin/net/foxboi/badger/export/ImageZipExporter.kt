package net.foxboi.badger.export

import io.ktor.http.*
import net.foxboi.badger.Badger
import net.foxboi.badger.EngineException
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.dyn.ScopeStack
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Abstract exporter for [Batch]es to ZIPs of image files.
 */
abstract class ImageZipExporter(
    val format: EncodedImageFormat,
    val quality: Int = 100
) : Exporter<Batch> {
    override val contentType = ContentType.Application.Zip

    override suspend fun export(
        element: Batch,
        stack: ScopeStack,
        assets: AssetManager
    ): Exportable {
        val zip = Badger.zip.getBuilder()
        val cache = TemplateCache(assets)

        zip.use {
            for ((name, entry) in element.entries) {
                val bmp = drawEntryToBitmap(element, entry, stack, assets, cache) ?: continue
                val png = Image.makeFromBitmap(bmp).encodeToData(format, quality)
                    ?: throw EngineException("Failed to convert to $format")

                zip.add("$name.png") {
                    it.write(png.bytes)
                }
            }
        }

        return zip
    }
}