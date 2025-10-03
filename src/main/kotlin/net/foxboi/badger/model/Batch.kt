package net.foxboi.badger.model

import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value
import net.foxboi.badger.model.dyn.LocalScope

/**
 * A [Batch] is a listing of [Template]s that can be exported together. A [Batch] consists of a list of [entries](Entry)
 * and a [local scope](LocalScope) that takes precedence over the local scope of a template, even within the template,
 * allowing batches to override variables defined in the template.
 */
class Batch {
    val scope = LocalScope()
    val entries = mutableMapOf<String, BatchEntry>()

    fun set(name: String, value: Value<*>) {
        scope.set(name, value)
    }

    fun set(name: String, value: Expr) {
        scope.set(name, value)
    }

    fun unset(name: String) {
        scope.unset(name)
    }

    fun addEntry(name: String, entry: BatchEntry) {
        entries[name] = entry
    }
}