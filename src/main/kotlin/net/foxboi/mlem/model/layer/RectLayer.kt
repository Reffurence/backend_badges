package net.foxboi.mlem.model.layer

import net.foxboi.mlem.graphics.Context
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.model.dyn.Scope
import net.foxboi.mlem.util.Color
import net.foxboi.mlem.util.Length

class RectLayer(
    val x: Dyn<Length>,
    val y: Dyn<Length>,
    val w: Dyn<Length>,
    val h: Dyn<Length>,
    val color: Dyn<Color>,
) : Layer() {
    override suspend fun drawLayer(
        ctx: Context,
        scope: Scope,
        template: Template
    ) {
        val w = template.toX(w via scope)
        val h = template.toY(h via scope)

        val x = template.toX(x via scope, w)
        val y = template.toY(y via scope, h)

        val col = color via scope

        ctx.apply {
            color = col.argb

            drawRect(x, y, w, h)
        }
    }
}