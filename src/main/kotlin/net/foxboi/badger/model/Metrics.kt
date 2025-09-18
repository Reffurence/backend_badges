package net.foxboi.badger.model

import kotlin.math.round

/**
 * The metrics of a [Template]. The metrics determines the size of a template in both pixels and inches, and
 * consequently it determines how lengths in a template convert to pixels.
 */
class Metrics private constructor(
    val wdtPx: Int,
    val hgtPx: Int,
    val wdtIn: Double,
    val hgtIn: Double,
    val wdtDpi: Double,
    val hgtDpi: Double
) {
    companion object {
        fun fromPxIn(wdtPx: Int, hgtPx: Int, wdtIn: Double, hgtIn: Double) = Metrics(
            wdtPx, hgtPx,
            wdtIn, hgtIn,
            wdtPx / wdtIn, hgtPx / hgtIn
        )

        fun fromInDpi(wdtIn: Double, hgtIn: Double, dpi: Double) = fromInDpi(
            wdtIn, hgtIn,
            dpi, dpi
        )

        fun fromInDpi(wdtIn: Double, hgtIn: Double, wdtDpi: Double, hgtDpi: Double) = fromPxIn(
            round(wdtIn * wdtDpi).toInt(),
            round(hgtIn * hgtDpi).toInt(),
            wdtIn,
            hgtIn
        )

        fun fromPxDpi(wdtPx: Int, hgtPx: Int, dpi: Double) = fromPxDpi(wdtPx, hgtPx, dpi, dpi)

        fun fromPxDpi(wdtPx: Int, hgtPx: Int, wdtDpi: Double, hgtDpi: Double) = fromPxIn(
            wdtPx, hgtPx,
            wdtPx / wdtDpi, hgtPx / hgtDpi
        )

        fun digital(wdtPx: Int, hgtPx: Int) = fromPxDpi(wdtPx, hgtPx, 72.0)
    }
}