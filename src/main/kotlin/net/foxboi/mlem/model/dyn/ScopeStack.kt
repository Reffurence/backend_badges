package net.foxboi.mlem.model.dyn

import net.foxboi.mlem.expr.Expr

class ScopeStack : Scope {
    private val scopes = ArrayDeque<Scope>()

    override fun get(name: String): Expr? {
        for (scope in scopes) {
            val v = scope.get(name)
            if (v != null) return v
        }
        return null
    }

    fun pushFront(scope: Scope) {
        scopes.addFirst(scope)
    }

    fun pushBack(scope: Scope) {
        scopes.addLast(scope)
    }

    fun popFront() {
        scopes.removeFirst()
    }

    fun popBack() {
        scopes.removeLast()
    }

    inline fun <R> withFront(scope: Scope, action: (ScopeStack) -> R): R {
        try {
            pushFront(scope)
            return action(this)
        } finally {
            popFront()
        }
    }

    inline fun <R> withBack(scope: Scope, action: (ScopeStack) -> R): R {
        try {
            pushBack(scope)
            return action(this)
        } finally {
            popBack()
        }
    }
}