package net.foxboi.mlem.expr

data object BoolType : Type<Boolean> {
    override val rank = TypeRank.BOOL
    override val name = "bool"

    val falseValue = Value(this, false)
    val trueValue = Value(this, true)

    override fun new(value: Boolean): Value<Boolean> {
        return if (value) trueValue else falseValue
    }

    override fun truth(value: Boolean): Boolean {
        return value
    }

    override fun string(value: Boolean): String {
        return if (value) "true" else "false"
    }

    override fun <B : Any> convert(from: Value<B>): Boolean {
        return from.truth
    }

    override fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
        return Compare.compare(this, lhs, rhs)
    }

    override fun bitAnd(lhs: Value<*>, rhs: Value<*>): Value<*> {
        return Value.logicAnd(lhs, rhs)
    }

    override fun bitOr(lhs: Value<*>, rhs: Value<*>): Value<*> {
        return Value.logicOr(lhs, rhs)
    }

    override fun bitXor(lhs: Value<*>, rhs: Value<*>): Value<*> {
        return Value.logicXor(lhs, rhs)
    }

    override fun bitNot(value: Value<*>): Value<*> {
        return Value.logicNot(value)
    }
}