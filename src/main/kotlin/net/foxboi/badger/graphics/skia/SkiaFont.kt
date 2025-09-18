package net.foxboi.badger.graphics.skia

import net.foxboi.badger.graphics.Font
import org.jetbrains.skia.Typeface

/**
 * A [Font] for [SkiaContext]
 */
internal class SkiaFont(val typeface: Typeface) : Font {
    override val name get() = typeface.familyName
}
