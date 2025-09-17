package net.foxboi.mlem.model.dyn

import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.Value

interface Scope {
    fun get(name: String): Expr?
}

fun Scope.eval(expr: Expr): Value<*> {
    return evalRec(expr, mutableMapOf(), mutableSetOf())
}

private fun Scope.resolveRec(
    name: String,
    resolved: MutableMap<String, Value<*>?>,
    resolving: MutableSet<String>
): Value<*>? {
    if (!resolving.add(name)) {
        throw DynException("Variable '$name' is dependent on itself")
    }

    val res = resolved.getOrPut(name) {
        get(name)?.let {
            evalRec(it, resolved, resolving)
        }
    }

    resolving.remove(name)

    return res
}

private fun Scope.evalRec(
    expr: Expr,
    resolved: MutableMap<String, Value<*>?>,
    resolving: MutableSet<String>
): Value<*> {
    return expr.eval {
        resolveRec(it, resolved, resolving)
    }
}