package net.foxboi.badger.export

import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports batces to a ZIP of WEBPs.
 */
object WebpBulkZip : ImageBulkZipExporter(
    EncodedImageFormat.WEBP
)