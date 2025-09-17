package net.foxboi.mlem.graphics

import kotlinx.serialization.Serializable

/**
 * Stroke style.
 * @param width The width of the stroke, in pixels.
 * @param join The line join type (see [Join]).
 * @param cap The line cap type (see [Cap]).
 * @param miterLimit The max size of a [Join.MITER] line join, in pixels. Miters exceeding this limit will be drawn
 *   as bevels.
 */
@Serializable
class Stroke(
    val width: Double,
    val join: Join = Join.MITER,
    val cap: Cap = Cap.FLAT,
    val miterLimit: Double = 2.0
) {
    companion object {
        /**
         * No stroke, i.e. a fill instead.
         */
        fun none(): Stroke? = null

        /**
         * A solid stroke of given width, with default join and cap properties.
         */
        fun solid(width: Double): Stroke = Stroke(width)
    }
}
