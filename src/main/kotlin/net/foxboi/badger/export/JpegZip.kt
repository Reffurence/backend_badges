package net.foxboi.badger.export

import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports batces to a ZIP of JPEGs.
 */
object JpegZip : ImageZipExporter(
    EncodedImageFormat.JPEG,
    90
)