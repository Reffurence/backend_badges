package net.foxboi.badger.model

import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value
import net.foxboi.badger.graphics.Context
import net.foxboi.badger.model.dyn.LocalScope
import net.foxboi.badger.model.dyn.ScopeStack
import net.foxboi.badger.model.layer.Layer
import net.foxboi.badger.util.Length

/**
 * An image template. Templates consist of layers and a [local scopae](LocalScope). The scope of a template is the
 * lowest, batches, entries and routing can override variables defined in a template.
 */
class Template(
    val metrics: Metrics,
    val scale: Double = 1.0
) {
    val wdt = metrics.wdtPx
    val hgt = metrics.hgtPx

    val layers = mutableListOf<Layer>()
    val scope = LocalScope()

    /**
     * Converts the given length to pixels using horizontal metrics.
     */
    fun toX(len: Length, selfWdt: Double = 0.0): Double {
        return len.toPixels(metrics.wdtDpi, wdt.toDouble(), selfWdt) * scale
    }

    /**
     * Converts the given length to pixels using vertical metrics.
     */
    fun toY(len: Length, selfHgt: Double = 0.0): Double {
        return len.toPixels(metrics.hgtDpi, hgt.toDouble(), selfHgt) * scale
    }

    /**
     * Draws this template using the given [drawing context](Context).
     *
     * @param ctx   The [Context] to draw with.
     * @param stack The [ScopeStack] to use. It will be modified but it's reset to its original state after use.
     */
    suspend fun draw(ctx: Context, stack: ScopeStack) {
        stack.withBack(scope) {
            for (layer in layers) {
                layer.draw(ctx, it, this)
            }
        }
    }

    /**
     * Adds a layer on top of all other layers.
     */
    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    /**
     * Removes a layer from this template.
     */
    fun removeLayer(layer: Layer) {
        layers.remove(layer)
    }

    /**
     * Sets a variable in the local scope of this template.
     */
    fun set(name: String, value: Value<*>) {
        scope.set(name, value)
    }

    /**
     * Sets a variable in the local scope of this template.
     */
    fun set(name: String, value: Expr) {
        scope.set(name, value)
    }

    /**
     * Unsets a variable in the local scope of this template.
     */
    fun unset(name: String) {
        scope.unset(name)
    }
}