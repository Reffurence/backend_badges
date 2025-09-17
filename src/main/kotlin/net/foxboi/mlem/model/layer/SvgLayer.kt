package net.foxboi.mlem.model.layer

import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.graphics.Context
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.model.dyn.Scope
import net.foxboi.mlem.util.Length
import kotlin.math.ceil

class SvgLayer(
    val x: Dyn<Length>,
    val y: Dyn<Length>,
    val w: Dyn<Length>,
    val h: Dyn<Length>,
    val image: Dyn<Asset>,
) : Layer() {
    override suspend fun drawLayer(
        ctx: Context,
        scope: Scope,
        template: Template
    ) {
        val w = template.toX(w via scope)
        val h = template.toY(h via scope)

        val img = image via scope
        val image = ctx.loadSvg(img, ceil(w).toInt(), ceil(h).toInt())

        val x = template.toX(x via scope, w)
        val y = template.toY(y via scope, h)

        ctx.apply {
            drawImage(image, x, y, w, h)
        }
    }
}