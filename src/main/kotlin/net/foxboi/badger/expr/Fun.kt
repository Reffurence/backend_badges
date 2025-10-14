package net.foxboi.badger.expr

fun interface Fun {
    fun call(args: List<Value<*>>): Value<*>
}