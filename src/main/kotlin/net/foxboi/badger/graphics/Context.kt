package net.foxboi.badger.graphics

import net.foxboi.badger.asset.Asset

/**
 * A badge graphics context.
 */
interface Context {
    /**
     * Load an image from a resource. The returned [Image] object can only be used with this context and any
     * forked context. It is not expected to work with other contexts.
     */
    suspend fun loadImage(location: Asset): Image

    /**
     * Load an svg from a resource and draw it to an image of the provided size.
     * The returned [Image] object can only be used with this context and any forked context. It is not expected to work
     * with other contexts.
     */
    suspend fun loadSvg(location: Asset, wdt: Int, hgt: Int): Image

    /**
     * Load a font from a resource. The returned [Font] object can only be used with this context and any
     * forked context. It is not expected to work with other contexts.
     */
    suspend fun loadFont(location: Asset): Font

    /**
     * The current [OverlayMode].
     */
    var overlayMode: OverlayMode

    /**
     * The current [HAlign] for text drawing.
     */
    var hAlign: HAlign

    /**
     * The current [VAlign] for text drawing.
     */
    var vAlign: VAlign

    /**
     * The current paint color, encoded ARGB in an [UInt].
     */
    var color: UInt

    /**
     * The current [Stroke]. If null, the context is in fill mode.
     */
    var stroke: Stroke?

    /**
     * Draw an image with its native width and height at the given coordinates.
     * @param image The image to draw.
     * @param x The leftmost coordinate of the image, in pixels.
     * @param y The topmost coordinate of the image, in pixels.
     */
    fun drawImage(image: Image, x: Double, y: Double)

    /**
     * Draw an image with given width and height at the given coordinates, scaling if the image is not the same size.
     * @param image The image to draw.
     * @param x The leftmost coordinate of the image, in pixels.
     * @param y The topmost coordinate of the image, in pixels.
     * @param w The width to draw the image with, in pixels.
     * @param h The height to draw the image with, in pixels.
     */
    fun drawImage(image: Image, x: Double, y: Double, w: Double, h: Double)

    /**
     * Creates a [Text] object with given font, text and size. This [Text] object can then be drawn over and over
     * using [drawText].
     *
     * The [Text] object is expected to work only with this context and any forked context.
     * It should not be used with other contexts.
     * @param font The [Font] to use.
     * @param text The text.
     * @param size The size of the text, in pixels.
     */
    fun createText(font: Font, text: String, size: Double): Text

    /**
     * Draws a [Text] object previously returned from [createText]. The text is anchored to the given coordinates, and
     * its alignment is determined by [hAlign] and [vAlign].
     *
     * @param text The [Text] to draw.
     * @param x The X coordinate of the anchor, in pixels.
     * @param y The Y coordinate of the anchor, in pixels.
     */
    fun drawText(text: Text, x: Double, y: Double)

    fun drawRect(x: Double, y: Double, w: Double, h: Double)

    /**
     * Forks this context for a new, empty canvas of given dimensions. It will supply this context to [drawing], in
     * which it can be drawn to. Once done, the drawn canvas will be returned as an [Image]. The returned [Image] object
     * is only valid in this and forked contexts, it should not be used in other contexts.
     *
     * Note that the forked context may be expected to be only valid within the scope of the [drawing] call. It should
     * not be stored for later drawing calls, as the returned [Image] will not update and exceptions may occur.
     *
     * @param w The width, in pixels.
     * @param h The height, in pixels.
     * @param drawing The drawing action.
     * @return The drawn [Image] object.
     */
    suspend fun drawToImage(w: Int, h: Int, drawing: suspend Context.() -> Unit): Image
}
