package net.foxboi.badger.model.layer

import net.foxboi.badger.asset.Asset
import net.foxboi.badger.graphics.Context
import net.foxboi.badger.graphics.shape.RRect
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.util.Length

/**
 * A layer that draws an image.
 *
 * @param x     The x coordinate to draw the image at.
 * @param y     The y coordinate to draw the image at.
 * @param w     The width to draw the image with.
 * @param h     The height to draw the image with.
 * @param image The image asset.
 */
class ImageLayer(
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
        val img = image via scope
        val image = ctx.loadImage(img)

        val w = template.toX(w via scope, image.w.toDouble())
        val h = template.toY(h via scope, image.h.toDouble())

        val x = template.toX(x via scope, w)
        val y = template.toY(y via scope, h)

        val r = template.toX(r via scope, h)

        ctx.apply {
            drawImage(image, RRect(x, y, w, h, r))
        }
    }
}