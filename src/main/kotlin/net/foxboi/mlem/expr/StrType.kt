package net.foxboi.mlem.expr

data object StrType : Type<String> {
    override val rank = TypeRank.STR
    override val name = "str"

    override fun truth(value: String): Boolean {
        return value.isNotEmpty()
    }

    override fun string(value: String): String {
        return value
    }

    override fun <B : Any> convert(from: Value<B>): String {
        return from.string
    }

    override fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
        return Compare.compare(this, lhs, rhs)
    }


    override fun add(lhs: Value<*>, rhs: Value<*>): Value<*> {
        return new(lhs.string + rhs.string)
    }

    override fun abs(value: Value<*>): Value<*> {
        return IntType.new(value.string.length)
    }
}