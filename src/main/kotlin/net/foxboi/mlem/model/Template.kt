package net.foxboi.mlem.model

import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.Value
import net.foxboi.mlem.graphics.Context
import net.foxboi.mlem.model.dyn.LocalScope
import net.foxboi.mlem.model.dyn.ScopeStack
import net.foxboi.mlem.model.layer.Layer
import net.foxboi.mlem.util.Length

class Template(
    val metrics: Metrics,
    val scale: Double = 1.0
) {
    val wdt = metrics.wdtPx
    val hgt = metrics.hgtPx

    val layers = mutableListOf<Layer>()
    val scope = LocalScope()

    fun toX(len: Length, selfWdt: Double = 0.0): Double {
        return len.toPixels(metrics.wdtDpi, wdt.toDouble(), selfWdt) * scale
    }

    fun toY(len: Length, selfHgt: Double = 0.0): Double {
        return len.toPixels(metrics.hgtDpi, hgt.toDouble(), selfHgt) * scale
    }

    suspend fun draw(ctx: Context, stack: ScopeStack) {
        stack.withBack(scope) {
            for (layer in layers) {
                layer.draw(ctx, it, this)
            }
        }
    }

    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    fun removeLayer(layer: Layer) {
        layers.remove(layer)
    }

    fun set(name: String, value: Value<*>) {
        scope.set(name, value)
    }

    fun set(name: String, value: Expr) {
        scope.set(name, value)
    }

    fun unset(name: String) {
        scope.unset(name)
    }
}