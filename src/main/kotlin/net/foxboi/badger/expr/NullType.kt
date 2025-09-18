package net.foxboi.badger.expr

data object NullType : Type<Unit> {
    override val rank = TypeRank.NULL
    override val name = "null"

    val inst = Value(this, Unit)

    override fun new(value: Unit): Value<Unit> {
        return inst
    }

    override fun truth(value: Unit): Boolean {
        return false
    }

    override fun string(value: Unit): String {
        return "null"
    }

    override fun <B : Any> convert(from: Value<B>) {
        // N/A; the return of Unit is completely implied
    }
}