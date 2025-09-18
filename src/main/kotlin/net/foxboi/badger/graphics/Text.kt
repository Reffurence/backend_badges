package net.foxboi.badger.graphics

/**
 * A blob of pre-shaped text. Typically this already encodes where glyphs should be relative to eachother, making it
 * more performant to repeatedly draw.
 */
interface Text {
    /**
     * The width of the text blob.
     */
    val width: Double

    /**
     * The height of the text blob.
     */
    val height: Double

    /**
     * Locates at what offset the text should be drawn to align correctly given the [HAlign].
     */
    fun xAnchor(align: HAlign): Double

    /**
     * Locates at what offset the text should be drawn to align correctly given the [VAlign].
     */
    fun yAnchor(align: VAlign): Double
}
