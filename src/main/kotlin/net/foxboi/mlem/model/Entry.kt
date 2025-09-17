package net.foxboi.mlem.model

import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.Value
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.model.dyn.LocalScope

class Entry(
    val template: Dyn<Asset>,
    val condition: Dyn<Boolean> = Dyn.const(true)
) {
    val scope = LocalScope()

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