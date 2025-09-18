package net.foxboi.badger.export

import org.jetbrains.skia.EncodedImageFormat

/**
 * Exports batces to a ZIP of PNGs.
 */
object PngZip : ImageZipExporter(
    EncodedImageFormat.PNG
)