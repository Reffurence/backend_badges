package net.foxboi.badger.model.layer

import net.foxboi.badger.graphics.Context
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.Scope

abstract class Layer {
    var name: String? = null
    var visible: Dyn<Boolean> = Dyn.const(true)

    protected abstract suspend fun drawLayer(ctx: Context, scope: Scope, template: Template)

    suspend fun draw(ctx: Context, scope: Scope, template: Template) {
        if (visible via scope) {
            drawLayer(ctx, scope, template)
        }
    }
}