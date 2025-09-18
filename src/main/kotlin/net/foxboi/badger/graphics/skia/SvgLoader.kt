package net.foxboi.badger.graphics.skia

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.svg.SVGDOM

internal class SvgLoader(val context: SkiaContext) {
    fun loadSvg(bytes: ByteArray, wdt: Int, hgt: Int): SkiaImage {
        val data = Data.makeFromBytes(bytes)
        val dom = SVGDOM(data)

        dom.setContainerSize(wdt.toFloat(), hgt.toFloat())

        val bmp = Bitmap()
        if (!bmp.allocN32Pixels(wdt, hgt)) {
            throw RuntimeException("Failed to allocate image of size $wdt x $hgt")
        }

        val canvas = Canvas(bmp)
        dom.render(canvas)

        val img = Image.makeFromBitmap(bmp)
        return SkiaImage(img)
    }
}