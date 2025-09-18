package net.foxboi.badger.model.layer

import net.foxboi.badger.asset.Asset
import net.foxboi.badger.graphics.Context
import net.foxboi.badger.graphics.HAlign
import net.foxboi.badger.graphics.VAlign
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.util.Color
import net.foxboi.badger.util.Length

/**
 * A layer that draws text. When `scaleToWidth` is specified, the text will be scaled down to fit this width if the
 * text wider than this width.
 *
 * @param text         The text to draw.
 * @param font         The asset of the font to draw the text with.
 * @param x            The x coordinate of the text anchor.
 * @param y            The y coordinate of the text anchor.
 * @param size         The font size.
 * @param hAlign       The horizontal text alignment, relative to the anchor point.
 * @param vAlign       The vetical text alignment, relative to the anchor point.
 * @param scaleToWidth An optional max width, text will be scaled down if wider.
 * @param color        The text color.
 */
class TextLayer(
    val text: Dyn<String>,
    val font: Dyn<Asset>,
    val x: Dyn<Length>,
    val y: Dyn<Length>,
    val size: Dyn<Length>,
    val hAlign: Dyn<HAlign>,
    val vAlign: Dyn<VAlign>,
    val scaleToWidth: Dyn<Length?>,
    val color: Dyn<Color>
) : Layer() {
    override suspend fun drawLayer(
        ctx: Context,
        scope: Scope,
        template: Template
    ) {
        val string = text via scope
        val font = ctx.loadFont(font via scope)
        val col = color via scope

        val x = template.toX(x via scope)
        val y = template.toY(y via scope)

        var size = template.toY(size via scope)

        var text = ctx.createText(font, string, size)

        val fit = scaleToWidth via scope
        if (fit != null) {
            val max = template.toX(fit)
            var ratio = text.width / max
            if (ratio > 1) {
                size /= ratio
                text = ctx.createText(font, string, size)
                ratio = text.width / max
            }
        }

        ctx.hAlign = hAlign via scope
        ctx.vAlign = vAlign via scope

        ctx.apply {
            color = col.argb
            ctx.drawText(text, x, y)
        }
    }
}