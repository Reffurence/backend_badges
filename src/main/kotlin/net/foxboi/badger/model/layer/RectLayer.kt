package net.foxboi.badger.model.layer

import net.foxboi.badger.graphics.Context
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.util.Color
import net.foxboi.badger.util.Length

/**
 * A layer that fills a rectangle with a solid color.
 *
 * @param x     The x coordinate to draw the rectangle at.
 * @param y     The y coordinate to draw the rectangle at.
 * @param w     The width of the rectangle.
 * @param h     The height of the rectangle.
 * @param color The fill color.
 */
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