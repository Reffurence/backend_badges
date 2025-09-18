package net.foxboi.badger.model.dyn

import net.foxboi.badger.expr.Expr

/**
 * A mutable stack of [Scope]s, which itself acts as a scope.
 * When getting a variable from a [ScopeStack], it will go through the scopes in the stack from front to back, trying to
 * get the variable from each scope in the stack until it finds a scope which has the variable defined.
 */
class ScopeStack : Scope {
    private val scopes = ArrayDeque<Scope>()

    override fun get(name: String): Expr? {
        for (scope in scopes) {
            val v = scope.get(name)
            if (v != null) return v
        }
        return null
    }

    /**
     * Add a scope in front of the stack, to be attempted before any other scope in the stack.
     */
    fun pushFront(scope: Scope) {
        scopes.addFirst(scope)
    }

    /**
     * Add a scope in the back of the stack, to be attempted after any other scope in the stack.
     */
    fun pushBack(scope: Scope) {
        scopes.addLast(scope)
    }

    /**
     * Remove a scope in front of the stack. Throw [NoSuchElementException] when the stack is empty.
     */
    fun popFront() {
        scopes.removeFirst()
    }

    /**
     * Remove a scope in the back of the stack. Throw [NoSuchElementException] when the stack is empty.
     */
    fun popBack() {
        scopes.removeLast()
    }

    /**
     * Pushes the given scope to the front of the stack, then calls the given closure, and then pops the scope from the
     * stack again.
     */
    inline fun <R> withFront(scope: Scope, action: (ScopeStack) -> R): R {
        try {
            pushFront(scope)
            return action(this)
        } finally {
            popFront()
        }
    }

    /**
     * Pushes the given scope to the back of the stack, then calls the given closure, and then pops the scope from the
     * stack again.
     */
    inline fun <R> withBack(scope: Scope, action: (ScopeStack) -> R): R {
        try {
            pushBack(scope)
            return action(this)
        } finally {
            popBack()
        }
    }
}