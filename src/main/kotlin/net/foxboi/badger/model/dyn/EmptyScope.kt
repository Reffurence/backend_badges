package net.foxboi.badger.model.dyn

import net.foxboi.badger.expr.Expr

/**
 * A [Scope] without any variables.
 */
object EmptyScope : Scope {
    override fun get(name: String): Expr? = null
}