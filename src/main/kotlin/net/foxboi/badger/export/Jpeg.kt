package net.foxboi.badger.export

import io.ktor.http.*
import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports templates to JPEG.
 */
object Jpeg : ImageExporter(
    ContentType.Image.JPEG,
    EncodedImageFormat.JPEG,
    90
)