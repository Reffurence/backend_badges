package net.foxboi.mlem.util

import kotlin.math.abs


private const val INCHES_PER_MILLIMETRE = 5.0 / 127.0
private const val INCHES_PER_CENTIMETRE = 50.0 / 127.0
private const val INCHES_PER_METRE = 5000.0 / 127.0

private const val INCHES_PER_FOOT = 12.0
private const val INCHES_PER_YARD = 36.0

private const val INCHES_PER_PICA = 1.0 / 6.0
private const val INCHES_PER_POINT = 1.0 / 72.0

/**
 * A length, to be used as a coordinate or dimension. Lengths indicate an amount of pixels, which may be dependent on
 * context parameters.
 */
class Length private constructor(
    /**
     * The part of the length in absolute pixels.
     */
    val pixels: Double,

    /**
     * The part of the length in inches. This represents all imperial and metric lengths as they relate to eachother by
     * fixed ratios.
     * This converts to pixels given the amount of 'dots per inch' (DPI): `pixels = inches * dpi`.
     */
    val inches: Double,

    /**
     * The part of the length in percentage of the parent's size. This converts to pixels given the size of the parent:
     * `pixels = parentPerc / 100 * parentSize`.
     */
    val parentPerc: Double,

    /**
     * The part of the length in percentage of the object's own size. This converts to pixels given the size of the
     * object itself: `pixels = selfPerc / 100 * selfSize`.
     */
    val selfPerc: Double
) {
    companion object {
        /**
         * Zero units of length.
         */
        val zero = Length(0.0, 0.0, 0.0, 0.0)

        /**
         * One pixel of length.
         */
        val onePixel = Length(1.0, 0.0, 0.0, 0.0)

        /**
         * Creates a new length. The returned length is the sum of all lengths given in as arguments.
         *
         * Example usage:
         * ```kotlin
         * Length.of(pixels = 100)          // 100 px
         * Length.of(centimetres = 5)       // 5 cm
         * Length.of(feet = 6, inches = 4)  // 6'4"
         * Length.of(points = 12)           // 9 pt
         * ```
         *
         * @param pixels An amount of pixels (px).
         * @param inches An amount of inches (in).
         * @param feet An amount of feet (1 ft = 12 in).
         * @param yards An amount of feet (1 yd = 36 in).
         * @param millimetres An amount of millimetres (1 mm = 5/127 in).
         * @param centimetres An amount of centimetres (1 cm = 50/127 in).
         * @param metres An amount of metres (1 m = 5000/127 in).
         * @param points An amount of points (1 pt = 1/72 in).
         * @param pica An amount of pica (1 pc = 1/6 in).
         * @param parentPerc A percentage of the size of the object's parent.
         * @param selfPerc A percentage of the size of the object itself.
         * @return The sum of all given lengths.
         */
        fun of(
            pixels: Double = 0.0,
            inches: Double = 0.0,
            feet: Double = 0.0,
            yards: Double = 0.0,
            millimetres: Double = 0.0,
            centimetres: Double = 0.0,
            metres: Double = 0.0,
            points: Double = 0.0,
            pica: Double = 0.0,
            parentPerc: Double = 0.0,
            selfPerc: Double = 0.0
        ): Length {
            return Length(
                pixels = pixels,
                inches = inches +
                        feet * INCHES_PER_FOOT +
                        yards * INCHES_PER_YARD +
                        millimetres * INCHES_PER_MILLIMETRE +
                        centimetres * INCHES_PER_CENTIMETRE +
                        metres * INCHES_PER_METRE +
                        points * INCHES_PER_POINT +
                        pica * INCHES_PER_PICA,
                parentPerc = parentPerc,
                selfPerc = selfPerc,
            )
        }

        fun pixels(pixels: Double): Length {
            return Length(pixels, 0.0, 0.0, 0.0)
        }

        fun inches(inches: Double): Length {
            return Length(0.0, inches, 0.0, 0.0)
        }

        fun feet(feet: Double): Length {
            return Length(0.0, feet * INCHES_PER_FOOT, 0.0, 0.0)
        }

        fun yards(yards: Double): Length {
            return Length(0.0, yards * INCHES_PER_YARD, 0.0, 0.0)
        }

        fun millimetres(millimetres: Double): Length {
            return Length(0.0, millimetres * INCHES_PER_MILLIMETRE, 0.0, 0.0)
        }

        fun centimetres(centimetres: Double): Length {
            return Length(0.0, centimetres * INCHES_PER_CENTIMETRE, 0.0, 0.0)
        }

        fun metres(metres: Double): Length {
            return Length(0.0, metres * INCHES_PER_METRE, 0.0, 0.0)
        }

        fun points(points: Double): Length {
            return Length(0.0, points * INCHES_PER_POINT, 0.0, 0.0)
        }

        fun pica(pica: Double): Length {
            return Length(0.0, pica * INCHES_PER_PICA, 0.0, 0.0)
        }

        fun parentPerc(parentPerc: Double): Length {
            return Length(0.0, 0.0, parentPerc, 0.0)
        }

        fun selfPerc(selfPerc: Double): Length {
            return Length(0.0, 0.0, 0.0, selfPerc)
        }
    }

    fun toPixels(dpi: Double, parentSize: Double, selfSize: Double): Double {
        return pixels +
                inches * dpi +
                parentPerc / 100.0 * parentSize +
                selfPerc / 100.0 * selfSize
    }

    fun toInches(dpi: Double, parentSize: Double, selfSize: Double): Double {
        return pixels / dpi +
                inches +
                parentPerc / 100.0 * parentSize / dpi +
                selfPerc / 100.0 * selfSize / dpi
    }

    fun isZero(): Boolean {
        return pixels == 0.0 && inches == 0.0 && parentPerc == 0.0 && selfPerc == 0.0
    }

    operator fun plus(other: Length): Length {
        return Length(
            this.pixels + other.pixels,
            this.inches + other.inches,
            this.parentPerc + other.parentPerc,
            this.selfPerc + other.selfPerc,
        )
    }

    operator fun minus(other: Length): Length {
        return Length(
            this.pixels - other.pixels,
            this.inches - other.inches,
            this.parentPerc - other.parentPerc,
            this.selfPerc - other.selfPerc,
        )
    }

    operator fun unaryPlus(): Length {
        return this
    }

    operator fun unaryMinus(): Length {
        return Length(
            -pixels,
            -inches,
            -parentPerc,
            -selfPerc
        )
    }

    operator fun times(scale: Double): Length {
        return Length(
            pixels * scale,
            inches * scale,
            parentPerc * scale,
            selfPerc * scale
        )
    }

    operator fun div(scale: Double): Length {
        return Length(
            pixels / scale,
            inches / scale,
            parentPerc / scale,
            selfPerc / scale
        )
    }

    override fun hashCode(): Int {
        var hash = pixels.hashCode()
        hash = hash * 31 + inches.hashCode()
        hash = hash * 31 + parentPerc.hashCode()
        hash = hash * 31 + selfPerc.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true

            other is Length -> pixels == other.pixels &&
                    inches == other.inches &&
                    parentPerc == other.parentPerc &&
                    selfPerc == other.selfPerc

            else -> false
        }
    }

    private var string: String? = null

    override fun toString(): String {
        if (string == null) {
            string = buildString {
                if (isZero()) {
                    append("0 px")
                } else {
                    var sign = false

                    if (pixels != 0.0) {
                        if (pixels < 0) {
                            append("- ")
                        }

                        append(abs(pixels))
                        append("px")

                        sign = true
                    }

                    if (inches != 0.0) {
                        if (sign) {
                            append(if (inches < 0) " - " else " + ")
                        } else if (inches < 0) {
                            append("- ")
                        }

                        append(abs(inches))
                        append("in")

                        sign = true
                    }

                    if (parentPerc != 0.0) {
                        if (sign) {
                            append(if (parentPerc < 0) " - " else " + ")
                        } else if (parentPerc < 0) {
                            append("- ")
                        }

                        append(abs(parentPerc))
                        append("par")

                        sign = true
                    }

                    if (selfPerc != 0.0) {
                        if (sign) {
                            append(if (selfPerc < 0) " - " else " + ")
                        } else if (selfPerc < 0) {
                            append("- ")
                        }

                        append(abs(selfPerc))
                        append("self")
                    }
                }
            }
        }

        return string!!
    }
}