package net.foxboi.mlem.model

import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.Value
import net.foxboi.mlem.model.dyn.LocalScope

class Batch {
    val scope = LocalScope()
    val entries = mutableMapOf<String, Entry>()

    fun set(name: String, value: Value<*>) {
        scope.set(name, value)
    }

    fun set(name: String, value: Expr) {
        scope.set(name, value)
    }

    fun unset(name: String) {
        scope.unset(name)
    }

    fun addEntry(name: String, entry: Entry) {
        entries[name] = entry
    }
}