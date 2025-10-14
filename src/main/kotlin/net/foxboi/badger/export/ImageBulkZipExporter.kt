package net.foxboi.badger.export

import io.ktor.http.*
import net.foxboi.badger.Badger
import net.foxboi.badger.EngineException
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.BulkInstance
import net.foxboi.badger.model.dyn.ScopeStack
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Abstract exporter for [Batch]es to ZIPs of image files.
 */
abstract class ImageBulkZipExporter(
    val format: EncodedImageFormat,
    val quality: Int = 100
) : Exporter<BulkInstance> {
    override val contentType = ContentType.Application.Zip

    override suspend fun export(
        element: BulkInstance,
        stack: ScopeStack,
        assets: AssetManager
    ): Exportable {
        val zip = Badger.zip.getBuilder()
        val cache = TemplateCache(assets)

        zip.use {
            element.iterateBatchEntries(stack, assets) { index, _, batch, name, entry, stack ->
                val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache)
                if (bmp != null) {
                    val png = Image.makeFromBitmap(bmp).encodeToData(format, quality)
                        ?: throw EngineException("Failed to convert to $format")

                    zip.add("$index/$name.png") {
                        it.write(png.bytes)
                    }
                }
            }
        }

        return zip
    }
}