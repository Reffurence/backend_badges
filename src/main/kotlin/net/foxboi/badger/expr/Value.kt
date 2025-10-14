package net.foxboi.badger.expr

data class Value<A : Any>(
    val type: Type<A>,
    val value: A
) {
    val truth: Boolean get() = type.truth(value)
    val string: String get() = type.string(value)

    fun isType(type: Type<*>): Boolean {
        return this.type == type
    }

    fun <B : Any> castValue(to: Type<B>): B? {
        return cast(to)?.value
    }

    fun <B : Any> cast(to: Type<B>): Value<B>? {
        @Suppress("UNCHECKED_CAST")
        return if (to == type) this as Value<B> else null
    }

    fun <B : Any> convertValue(to: Type<B>): B? {
        return convert(to)?.value
    }

    fun <B : Any> convert(to: Type<B>): Value<B>? {
        val cast = cast(to)
        if (cast != null) {
            return cast
        }

        val convert = to.convert(this)
        if (convert != null) {
            return Value(to, convert)
        }

        return null
    }

    /**
     * Map from type `P` to type `Q`, if this value is of type `P`.
     */
    inline fun <P : Any, Q : Any> map(from: Type<P>, to: Type<Q>, map: (P) -> Q): Value<Q>? {
        return to.new(map(castValue(from) ?: return null))
    }

    override fun toString(): String {
        return "${type.name} $string"
    }

    companion object {
        private fun max(lhsType: Type<*>, rhsType: Type<*>): Type<*> {
            val lhsRank = lhsType.rank
            val rhsRank = rhsType.rank

            return if (rhsRank > lhsRank) {
                rhsType
            } else {
                lhsType
            }
        }

        private fun min(lhsType: Type<*>, rhsType: Type<*>): Type<*> {
            val lhsRank = lhsType.rank
            val rhsRank = rhsType.rank

            return if (rhsRank <= lhsRank) {
                rhsType
            } else {
                lhsType
            }
        }

        private inline fun apply(lhs: Type<*>, rhs: Type<*>, action: Type<*>.() -> Value<*>?): Value<*>? {
            return action(max(lhs, rhs)) ?: action(min(lhs, rhs))
        }


        // Arithmetic

        fun plus(value: Value<*>): Value<*>? {
            return value.type.plus(value)
        }

        fun neg(value: Value<*>): Value<*>? {
            return value.type.neg(value)
        }

        fun abs(value: Value<*>): Value<*>? {
            return value.type.abs(value)
        }

        fun add(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { add(lhs, rhs) }
        }

        fun sub(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { sub(lhs, rhs) }
        }

        fun mul(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { mul(lhs, rhs) }
        }

        fun div(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { div(lhs, rhs) }
        }

        fun rem(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { rem(lhs, rhs) }
        }


        // Bitwise manipulation

        fun leftSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { leftSh(lhs, rhs) }
        }

        fun rightSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { rightSh(lhs, rhs) }
        }

        fun bitOr(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { bitOr(lhs, rhs) }
        }

        fun bitAnd(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { bitAnd(lhs, rhs) }
        }

        fun bitXor(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return apply(lhs.type, rhs.type) { bitXor(lhs, rhs) }
        }

        fun bitNot(value: Value<*>): Value<*>? {
            return value.type.bitNot(value)
        }


        // Logic

        fun logicOr(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs.truth || rhs.truth)
        }

        fun logicAnd(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs.truth && rhs.truth)
        }

        fun logicXor(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs.truth != rhs.truth)
        }

        fun logicEquiv(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs.truth == rhs.truth)
        }

        fun logicNot(value: Value<*>): Value<*> {
            return BoolType.new(!value.truth)
        }


        // Comparison

        fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
            // Note that comparison does not fall back on lower rank when the higher rank determines the values
            // are incomparable.
            return max(lhs.type, rhs.type).compare(lhs, rhs)
        }

        fun lt(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return BoolType.maybeNew(compare(lhs, rhs).lt)
        }

        fun gt(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return BoolType.maybeNew(compare(lhs, rhs).gt)
        }

        fun le(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return BoolType.maybeNew(compare(lhs, rhs).le)
        }

        fun ge(lhs: Value<*>, rhs: Value<*>): Value<*>? {
            return BoolType.maybeNew(compare(lhs, rhs).ge)
        }

        fun eq(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(compare(lhs, rhs).eq)
        }

        fun neq(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(!compare(lhs, rhs).eq)
        }

        fun same(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs == rhs)
        }

        fun notSame(lhs: Value<*>, rhs: Value<*>): Value<*> {
            return BoolType.new(lhs != rhs)
        }
    }
}