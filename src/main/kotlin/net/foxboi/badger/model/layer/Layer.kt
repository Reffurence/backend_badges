package net.foxboi.badger.model.layer

import net.foxboi.badger.graphics.Context
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope

/**
 * A layer of a [Template].
 */
abstract class Layer {
    /**
     * The internal name of the layer.
     */
    var name: String? = null

    /**
     * Whether the layer is visible.
     */
    var visible: Dyn<Boolean> = Dyn.const(true)

    /**
     * Draw this layer, given that the visibility check has already passed.
     *
     * @param ctx      The drawing context.
     * @param scope    The scope.
     * @param template The parent template.
     */
    protected abstract suspend fun drawLayer(ctx: Context, scope: Scope, template: Template)

    /**
     * Draw this layer, if it is visible.
     *
     * @param ctx      The drawing context.
     * @param scope    The scope.
     * @param template The parent template.
     */
    suspend fun draw(ctx: Context, scope: Scope, template: Template) {
        if (visible via scope) {
            drawLayer(ctx, scope, template)
        }
    }
}