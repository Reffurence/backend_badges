package net.foxboi.badger.export

import io.ktor.http.*
import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports templates to WEBP.
 */
object Webp : ImageExporter(
    ContentType.Image.WEBP,
    EncodedImageFormat.WEBP
)