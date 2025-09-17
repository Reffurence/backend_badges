package net.foxboi.mlem.expr

import net.foxboi.mlem.util.Length

data object LenType : Type<Length> {
    override val rank = TypeRank.UNIT
    override val name = "len"

    override fun truth(value: Length): Boolean {
        return !value.isZero()
    }

    override fun string(value: Length): String {
        return "$value"
    }

    override fun <B : Any> convert(from: Value<B>): Length? {
        return when (from.type) {
            NullType -> Length.zero
            LenType -> from.cast(LenType)?.value
            IntType -> from.cast(IntType)?.value?.let { Length.pixels(it.toDouble()) }
            FloatType -> from.cast(FloatType)?.value?.let { Length.pixels(it) }
            StrType -> from.cast(StrType)?.value?.let { Length.pixels(it.toDoubleOrNull() ?: Double.NaN) }

            else -> null
        }
    }


    override fun plus(value: Value<*>) = value

    override fun neg(value: Value<*>) = value.map(this, this) {
        -it
    }


    private fun arithmeticValue(value: Value<*>): Length? {
        return when (value.type) {
            BoolType -> value.castValue(BoolType)?.let { if (it) Length.onePixel else Length.zero }
            IntType -> value.castValue(IntType)?.let { Length.pixels(it.toDouble()) }
            FloatType -> value.castValue(FloatType)?.let { Length.pixels(it) }
            else -> value.castValue(LenType)
        }
    }

    private fun scalarValue(value: Value<*>): Double? {
        return when (value.type) {
            BoolType -> value.castValue(BoolType)?.let { if (it) 1.0 else 0.0 }
            IntType -> value.castValue(IntType)?.toDouble()
            else -> value.castValue(FloatType)
        }
    }

    override fun add(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue + rhsValue)
    }

    override fun sub(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue - rhsValue)
    }

    override fun mul(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        if (lhs.isType(this) && rhs.isType(this)) {
            // Multiplying lengths makes areas, we don't have area values
            return null
        }

        if (lhs.isType(this)) {
            val lhsVal = lhs.castValue(this) ?: return null
            val rhsVal = scalarValue(rhs) ?: return null

            return new(lhsVal * rhsVal)
        } else {
            val lhsVal = scalarValue(lhs) ?: return null
            val rhsVal = rhs.castValue(this) ?: return null

            return new(rhsVal * lhsVal)
        }
    }

    override fun div(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        if (lhs.isType(this) && rhs.isType(this)) {
            // Dividing lengths creates weird units like "pixels per inch" which we can't represent
            return null
        }

        if (lhs.isType(this)) {
            val lhsVal = lhs.castValue(this) ?: return null
            val rhsVal = scalarValue(rhs) ?: return null

            return new(lhsVal / rhsVal)
        } else {
            // Dividing numbers by lengths creates even weirder units like "inch ^ -1" which we definitely don't want
            // to represent
            return null
        }
    }
}