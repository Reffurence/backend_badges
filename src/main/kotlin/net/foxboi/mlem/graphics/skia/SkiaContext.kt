package net.foxboi.mlem.graphics.skia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.graphics.*
import net.foxboi.mlem.graphics.Font
import net.foxboi.mlem.graphics.Image
import org.jetbrains.skia.*
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.Font as SkFont
import org.jetbrains.skia.Image as SkImage

/**
 * A [Context] that uses Skia as backend. Be sure to set [canvas] before making drawing calls.
 */
class SkiaContext(
    private val assets: AssetManager
) : Context {
    lateinit var canvas: Canvas

    private val svg = SvgLoader(this)

    override suspend fun loadImage(location: Asset): Image {
        try {
            val img = SkImage.makeFromEncoded(assets.bytes(location))
            return SkiaImage(img)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load image", e)
        }
    }

    override suspend fun loadSvg(location: Asset, wdt: Int, hgt: Int): Image {
        try {
            return svg.loadSvg(assets.bytes(location), wdt, hgt)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load SVG", e)
        }
    }

    override suspend fun loadFont(location: Asset): Font {
        try {
            val font = Typeface.makeFromData(Data.makeFromBytes(assets.bytes(location)))
            return SkiaFont(font)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load font", e)
        }
    }

    private val Image.skia
        get(): SkiaImage {
            return this as SkiaImage
        }

    private val Font.skia
        get(): SkiaFont {
            return this as SkiaFont
        }

    private val Text.skia
        get(): SkiaText {
            return this as SkiaText
        }


    override var overlayMode = OverlayMode.NORMAL
    override var hAlign = HAlign.LEFT
    override var vAlign = VAlign.BASELINE
    override var color = 0xFF000000u
    override var stroke = Stroke.none()

    private val shapePaint = Paint()
        get() {
            applyBaseProperties(field)
            applyFillProperties(field)
            return field
        }

    private val imagePaint = Paint()
        get() {
            applyBaseProperties(field)
            applyImageProperties(field)
            return field
        }

    private fun applyBaseProperties(v: Paint) {
        v.blendMode = when (overlayMode) {
            OverlayMode.NORMAL -> BlendMode.SRC_OVER
            OverlayMode.MULTIPLY -> BlendMode.MULTIPLY
            OverlayMode.ADD -> BlendMode.PLUS
            OverlayMode.ERASE -> BlendMode.CLEAR
        }
    }

    private fun applyFillProperties(v: Paint) {
        v.color = color.toInt()

        val stroke = stroke
        v.setStroke(stroke != null)
        if (stroke != null) {
            v.strokeWidth = stroke.width.toFloat()
            v.strokeCap = when (stroke.cap) {
                Cap.FLAT -> PaintStrokeCap.BUTT
                Cap.SQUARE -> PaintStrokeCap.SQUARE
                Cap.ROUND -> PaintStrokeCap.ROUND
            }
            v.strokeJoin = when (stroke.join) {
                Join.BEVEL -> PaintStrokeJoin.BEVEL
                Join.MITER -> PaintStrokeJoin.MITER
                Join.ROUND -> PaintStrokeJoin.ROUND
            }
            v.strokeMiter = stroke.miterLimit.toFloat()
        }
    }

    private fun applyImageProperties(v: Paint) {
    }


    override fun drawImage(image: Image, x: Double, y: Double) {
        drawImage(image, x, y, image.w.toDouble(), image.h.toDouble())
    }

    override fun drawImage(image: Image, x: Double, y: Double, w: Double, h: Double) {
        val img = image.skia.image

        val src = Rect(0f, 0f, image.w.toFloat(), image.h.toFloat())
        val dst = Rect(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat())

        canvas.drawImageRect(img, src, dst, FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR), imagePaint, true)
    }


    override fun createText(font: Font, text: String, size: Double): Text {
        val shaper = Shaper.make()
        val skf = SkFont(font.skia.typeface, size.toFloat())
        val blob = shaper.shape(text, skf)!!

        return SkiaText(blob)
    }

    override fun drawText(text: Text, x: Double, y: Double) {
        val sk = text.skia

        val nx = x - text.xAnchor(hAlign)
        val ny = y - text.yAnchor(vAlign)

        canvas.drawTextBlob(sk.textBlob, nx.toFloat(), ny.toFloat(), shapePaint)
    }

    override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
        canvas.drawRect(
            Rect(
                x.toFloat(), y.toFloat(),
                (x + w).toFloat(), (y + h).toFloat()
            ), shapePaint
        )
    }

    override suspend fun drawToImage(w: Int, h: Int, drawing: suspend Context.() -> Unit): Image {
        return withContext(Dispatchers.Default) {
            val bmp = Bitmap()
            if (!bmp.allocN32Pixels(w, h)) {
                throw RuntimeException("Failed to allocate image of size $w x $h")
            }

            val context = SkiaContext(assets)
            context.canvas = Canvas(bmp)

            drawing(context)

            val img = SkImage.makeFromBitmap(bmp)
            SkiaImage(img)
        }
    }
}
