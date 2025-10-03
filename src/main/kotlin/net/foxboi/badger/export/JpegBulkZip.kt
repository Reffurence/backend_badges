package net.foxboi.badger.export

import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports batces to a ZIP of JPEGs.
 */
object JpegBulkZip : ImageBulkZipExporter(
    EncodedImageFormat.JPEG,
    90
)