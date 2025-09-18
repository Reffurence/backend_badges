package net.foxboi.badger.export

import io.ktor.http.*
import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports templates to PNG.
 */
object Png : ImageExporter(
    ContentType.Image.PNG,
    EncodedImageFormat.PNG
)