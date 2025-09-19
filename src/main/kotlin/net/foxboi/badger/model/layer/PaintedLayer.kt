package net.foxboi.badger.model.layer

import net.foxboi.badger.graphics.Context
import net.foxboi.badger.graphics.Stroke
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.model.paint.Paint
import net.foxboi.badger.model.paint.Fill as FillPaint
import net.foxboi.badger.model.paint.Stroke as StrokePaint

abstract class PaintedLayer : Layer() {
    val paints = mutableListOf<Paint>()

    protected inline fun Context.eachPaint(scope: Scope, template: Template, action: Context.() -> Unit) {
        for (paint in paints) {
            when (paint) {
                is FillPaint -> {
                    stroke = Stroke.none()
                    color = (paint.color via scope).argb
                }

                is StrokePaint -> {
                    stroke = Stroke(
                        template.toX(paint.width via scope),
                        paint.join via scope,
                        paint.cap via scope,
                        paint.miterLimit via scope
                    )
                    color = (paint.color via scope).argb
                }
            }

            action()
        }
    }
}