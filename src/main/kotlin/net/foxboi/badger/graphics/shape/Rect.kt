package net.foxboi.badger.graphics.shape

data class Rect(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
) : Shape {
    override val boundingRect
        get() = this
}