package net.foxboi.mlem.graphics.skia

import net.foxboi.mlem.graphics.HAlign
import net.foxboi.mlem.graphics.Text
import net.foxboi.mlem.graphics.VAlign
import org.jetbrains.skia.TextBlob

/**
 * A [Text] for [SkiaContext]
 */
internal class SkiaText(val textBlob: TextBlob) : Text {
    private val rect = textBlob.blockBounds

    override val width get() = rect.width.toDouble()
    override val height get() = rect.height.toDouble()

    override fun xAnchor(align: HAlign) = when (align) {
        HAlign.LEFT -> 0.0
        HAlign.CENTER -> rect.width / 2.0
        HAlign.RIGHT -> rect.width.toDouble()
    }

    override fun yAnchor(align: VAlign) = when (align) {
        VAlign.BOTTOM -> rect.height.toDouble()
        VAlign.BASELINE -> textBlob.firstBaseline.toDouble()
        VAlign.MIDDLE -> rect.height / 2.0
        VAlign.TOP -> 0.0
    }
}
