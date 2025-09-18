package net.foxboi.badger.model

import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value
import net.foxboi.badger.graphics.Context
import net.foxboi.badger.model.dyn.LocalScope
import net.foxboi.badger.model.dyn.ScopeStack
import net.foxboi.badger.model.layer.Layer
import net.foxboi.badger.util.Length

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