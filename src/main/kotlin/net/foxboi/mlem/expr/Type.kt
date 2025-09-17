package net.foxboi.mlem.expr

sealed interface Type<A : Any> {
    /**
     * The [rank](TypeRank) of this type.
     */
    val rank: TypeRank

    val name: String

    /**
     * Creates a new [Value] instance of this type.
     */
    fun new(value: A): Value<A> {
        return Value(this, value)
    }

    /**
     * Creates a new [Value] instance of this type, if the given value is not null. When the given value is null, this
     * method will simply return null too.
     */
    fun maybeNew(value: A?): Value<A>? {
        return Value(this, value ?: return null)
    }

    /**
     * Gets the truth value of a specific value of this type. Every value has a truth value, e.g., an empty string is
     * not truthy whereas a string with characters is.
     */
    fun truth(value: A): Boolean

    /**
     * Gets the stringified value of a specific value of this type.
     */
    fun string(value: A): String

    /**
     * Try to convert a given [Value] to this type. While casting fails if the value's type isn't exactly this type,
     * conversion may attempt to interpret the value as another type. For example, casting strings to ints will fail but
     * converting strings to ints will succeed as the string will be parsed as an integer.
     *
     * These conversions will always succeed:
     * - Converting to a string will always return the [string] value.
     * - Converting to a boolean will always return the [truth] value.
     * - Converting to null will always return the null value, completely ignoring the value.
     * - Conversion from null should always return a default value of this type.
     *
     * When the conversion fails, this method returns null (the Kotlin null).
     */
    fun <B : Any> convert(from: Value<B>): A?


    fun plus(value: Value<*>): Value<*>? {
        return null
    }

    fun neg(value: Value<*>): Value<*>? {
        return null
    }

    fun abs(value: Value<*>): Value<*>? {
        return null
    }

    fun add(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun sub(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun mul(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun div(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun rem(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun leftSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun rightSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun bitNot(value: Value<*>): Value<*>? {
        return null
    }

    fun bitAnd(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun bitOr(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }

    fun bitXor(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        return null
    }


    /**
     * Compares two values, at least one of which is this this type. This method returns one of 4 results:
     * - [Compare.EQUAL]: the two values are semantically equal.
     * - [Compare.LESS]: the left value is less than the right value.
     * - [Compare.GREATER]: the left value is greater than the right value.
     * - [Compare.INCOMPARABLE]: the values are inequal, but neither is less or greater than the other.
     */
    fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
        if (lhs.isType(this) && rhs.isType(this) && lhs.value == rhs.value) {
            return Compare.EQUAL
        }
        return Compare.INCOMPARABLE
    }
}