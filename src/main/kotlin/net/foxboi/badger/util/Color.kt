package net.foxboi.badger.util

import kotlin.math.max
import kotlin.math.min

private fun clamp(f: Float): Int {
    return clamp((f * 255).toInt())
}

private fun clamp(i: Int): Int {
    return max(min(i, 255), 0)
}

private fun clamp(u: UInt): Int {
    return clamp(u.toInt())
}

class Color {
    val ri: Int
    val gi: Int
    val bi: Int
    val ai: Int

    val rf get() = ri / 255f
    val gf get() = gi / 255f
    val bf get() = bi / 255f
    val af get() = ai / 255f

    val ru get() = ri.toUInt()
    val gu get() = gi.toUInt()
    val bu get() = bi.toUInt()
    val au get() = ai.toUInt()

    val argb get() = (au shl 24) or (ru shl 16) or (gu shl 8) or bu

    val rgb get() = (ru shl 16) or (gu shl 8) or bu

    constructor(rf: Float, gf: Float, bf: Float, af: Float = 1f) {
        ri = clamp(rf)
        gi = clamp(gf)
        bi = clamp(bf)
        ai = clamp(af)
    }

    constructor(ri: Int, gi: Int, bi: Int, ai: Int = 255) {
        this.ri = clamp(ri)
        this.gi = clamp(gi)
        this.bi = clamp(bi)
        this.ai = clamp(ai)
    }


    fun isFullyTransparent(): Boolean {
        return ai == 0
    }

    fun isFullyOpaque(): Boolean {
        return ai == 255
    }

    fun withAlpha(af: Float): Color {
        return Color(ri, gi, bi, (af * 255).toInt())
    }

    fun withAlpha(ai: Int): Color {
        return Color(ri, gi, bi, ai)
    }


    override fun toString(): String {
        if (isFullyOpaque()) {
            return "#%06X".format(rgb)
        }
        return "#%08X".format(argb)
    }

    override fun hashCode(): Int {
        var hash = ri.hashCode()
        hash = hash * 31 + gi.hashCode()
        hash = hash * 31 + bi.hashCode()
        hash = hash * 31 + ai.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true

            other is Color -> {
                ri == other.ri &&
                        gi == other.gi &&
                        bi == other.bi &&
                        ai == other.ai
            }

            else -> false
        }
    }


    companion object {
        val transparent = Color(0f, 0f, 0f, 0f)
        val black = Color(0f, 0f, 0f, 1f)

        fun fromArgb(argb: UInt): Color {
            val a = (argb shr 24) and 0xFFu
            val r = (argb shr 16) and 0xFFu
            val g = (argb shr 8) and 0xFFu
            val b = argb and 0xFFu

            return Color(
                r.toFloat() / 255f,
                g.toFloat() / 255f,
                b.toFloat() / 255f,
                a.toFloat() / 255f
            )
        }

        fun fromRgb(rgb: UInt): Color {
            val r = (rgb shr 16) and 0xFFu
            val g = (rgb shr 8) and 0xFFu
            val b = rgb and 0xFFu

            return Color(
                r.toFloat() / 255f,
                g.toFloat() / 255f,
                b.toFloat() / 255f,
                1f
            )
        }

        fun fromGrayscale(f: Float): Color {
            return Color(f, f, f)
        }

        fun fromGrayscale(i: Int): Color {
            return Color(i, i, i)
        }


        private fun hexit(c: Char) = when (c) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> c - '0'
            'a', 'b', 'c', 'd', 'e', 'f' -> c - 'a' + 10
            'A', 'B', 'C', 'D', 'E', 'F' -> c - 'A' + 10
            else -> -1
        }

        private fun coord(c1: Char, c2: Char): Float? {
            val h1 = hexit(c1)
            val h2 = hexit(c2)
            if (h1 < 0 || h2 < 0) return null
            return (h1 shl 4 or h2) / 255f
        }

        private fun coord(c: Char): Float? {
            return coord(c, c)
        }

        fun fromHexOrNull(hex: String): Color? {
            if (hex[0] != '#') return null

            return when (hex.length) {
                4 -> {
                    // #RGB
                    val r = coord(hex[1]) ?: return null
                    val g = coord(hex[2]) ?: return null
                    val b = coord(hex[3]) ?: return null
                    Color(r, g, b)
                }

                5 -> {
                    // #ARGB
                    val a = coord(hex[1]) ?: return null
                    val r = coord(hex[2]) ?: return null
                    val g = coord(hex[3]) ?: return null
                    val b = coord(hex[4]) ?: return null
                    Color(r, g, b, a)
                }

                7 -> {
                    // #RRGGBB
                    val r = coord(hex[1], hex[2]) ?: return null
                    val g = coord(hex[3], hex[4]) ?: return null
                    val b = coord(hex[5], hex[6]) ?: return null
                    Color(r, g, b)
                }

                9 -> {
                    // #AARRGGBB
                    val a = coord(hex[1], hex[2]) ?: return null
                    val r = coord(hex[3], hex[4]) ?: return null
                    val g = coord(hex[5], hex[6]) ?: return null
                    val b = coord(hex[7], hex[8]) ?: return null
                    Color(r, g, b, a)
                }

                else -> null
            }
        }

        fun fromHex(hex: String): Color {
            return fromHexOrNull(hex) ?: throw NumberFormatException("For input string: $hex")
        }
    }
}