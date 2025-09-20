package net.foxboi.badger.graphics.shape

data class RRect(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val r: Double
) : Shape {
    override val boundingRect = Rect(
        x, y,
        w, h
    )
}