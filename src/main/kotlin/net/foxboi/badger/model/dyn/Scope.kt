package net.foxboi.badger.model.dyn

import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value

/**
 * A [Scope] resolves variable names to [Expr] objects that define those variables.
 */
interface Scope {
    /**
     * Get a variable by name. If the variable is defined in this scope, then it returns a non-null [Expr] representing
     * the definition of that variable. If the variable is not defined, then `null` is returned.
     */
    fun get(name: String): Expr?
}

/**
 * Evaluates an [Expr] in this scope. This will ensure that all variables are fetched from this scope during the
 * evaluation of the specified expression and any other expressions that need to be evaluated as a result of it.
 */
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