package net.foxboi.mlem.model.layer

import net.foxboi.mlem.graphics.Context
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.model.dyn.Scope

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