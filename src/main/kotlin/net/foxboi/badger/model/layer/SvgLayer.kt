package net.foxboi.badger.model.layer

import net.foxboi.badger.asset.Asset
import net.foxboi.badger.graphics.Context
import net.foxboi.badger.graphics.shape.RRect
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.util.Length
import kotlin.math.ceil

/**
 * A layer that draws an SVG image. Note that the scaling of the SVG image is uniform, when the layer rectangle does not
 * match the SVG's aspect ratio, then the SVG image will be centered in the rectangle with the largest fitting size.
 *
 * @param x     The x coordinate to draw the image at.
 * @param y     The y coordinate to draw the image at.
 * @param w     The width to draw the image with.
 * @param h     The height to draw the image with.
 * @param image The SVG asset.
 */
class SvgLayer(
    val x: Dyn<Length>,
    val y: Dyn<Length>,
    val w: Dyn<Length>,
    val h: Dyn<Length>,
    val r: Dyn<Length> = Dyn.const(Length.zero),
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

        val r = template.toX(r via scope, h)

        ctx.apply {
            drawImage(image, RRect(x, y, w, h, r))
        }
    }
}