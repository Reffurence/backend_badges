package net.foxboi.badger.graphics.shape

data class Oval(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
) : Shape {
    override val boundingRect = Rect(
        x, y,
        w, h
    )
}