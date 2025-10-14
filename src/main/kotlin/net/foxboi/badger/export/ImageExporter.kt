package net.foxboi.badger.export

import io.ktor.http.*
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.ScopeStack
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Abstract exporter for [Template]s to image files.
 */
abstract class ImageExporter(
    override val contentType: ContentType,
    val format: EncodedImageFormat,
    val quality: Int = 100
) : Exporter<Template> {

    override suspend fun export(
        element: Template,
        stack: ScopeStack,
        assets: AssetManager
    ): Exportable {
        val bmp = drawTemplateToBitmap(element, stack, assets)

        val img = Image.makeFromBitmap(bmp)
        val data = img.encodeToData(format, quality)
            ?: throw RuntimeException("Failed to encode $format")

        return Exportable.bytes(data.bytes)
    }
}