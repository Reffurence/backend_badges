package net.foxboi.badger.export

import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports batces to a ZIP of WEBPs.
 */
object WebpZip : ImageZipExporter(
    EncodedImageFormat.WEBP
)