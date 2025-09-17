package net.foxboi.mlem.model.layer

import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.graphics.Context
import net.foxboi.mlem.graphics.HAlign
import net.foxboi.mlem.graphics.VAlign
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.model.dyn.Scope
import net.foxboi.mlem.util.Color
import net.foxboi.mlem.util.Length

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