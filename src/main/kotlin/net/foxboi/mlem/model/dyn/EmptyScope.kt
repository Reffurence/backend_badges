package net.foxboi.mlem.model.dyn

import net.foxboi.mlem.expr.Expr

/**
 * A [Scope] without any variables.
 */
object EmptyScope : Scope {
    override fun get(name: String): Expr? = null
}