package net.foxboi.badger.graphics.skia

import net.foxboi.badger.graphics.shape.Oval
import net.foxboi.badger.graphics.shape.RRect
import net.foxboi.badger.graphics.shape.Rect
import org.jetbrains.skia.RRect as SkRRect
import org.jetbrains.skia.Rect as SkRect

fun Rect.toSkiaRect(): SkRect {
    return SkRect.makeXYWH(
        x.toFloat(), y.toFloat(),
        w.toFloat(), h.toFloat()
    )
}

fun RRect.toSkiaRRect(): SkRRect {
    return SkRRect.makeXYWH(
        x.toFloat(), y.toFloat(),
        w.toFloat(), h.toFloat(),
        r.toFloat()
    )
}

fun Oval.toSkiaOval(): SkRect {
    return SkRect.makeXYWH(
        x.toFloat(), y.toFloat(),
        w.toFloat(), h.toFloat()
    )
}