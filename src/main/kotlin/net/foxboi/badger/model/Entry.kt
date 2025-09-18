package net.foxboi.badger.model

import net.foxboi.badger.asset.Asset
import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.LocalScope

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