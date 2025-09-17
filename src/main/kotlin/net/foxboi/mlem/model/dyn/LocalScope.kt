package net.foxboi.mlem.model.dyn

import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.Value

class LocalScope : Scope {
    private val variables = mutableMapOf<String, Expr>()

    fun set(name: String, value: Value<*>) {
        variables[name] = Expr.const(value)
    }

    fun set(name: String, value: Expr) {
        variables[name] = value
    }

    fun unset(name: String) {
        variables.remove(name)
    }

    override fun get(name: String): Expr? = variables[name]
}