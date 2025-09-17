package net.foxboi.mlem.graphics.skia

import net.foxboi.mlem.graphics.Image
import org.jetbrains.skia.Image as SImage

/**
 * An [Image] for [SkiaContext]
 */
internal class SkiaImage(val image: SImage) : Image {
    override val w get() = image.width
    override val h get() = image.height
}
