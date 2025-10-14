package net.foxboi.badger.expr

interface Evaluator {
    fun getVar(name: String): Value<*>?
    fun getFun(name: String): Fun?
}