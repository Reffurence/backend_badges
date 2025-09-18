package net.foxboi.badger.expr

import kotlin.math.absoluteValue

data object AngleType : Type<Double> {
    override val rank = TypeRank.UNIT
    override val name = "angle"

    fun new(value: Int): Value<Double> {
        return AngleType.new(value.toDouble())
    }

    fun new(value: Long): Value<Double> {
        return AngleType.new(value.toDouble())
    }

    fun new(value: Float): Value<Double> {
        return AngleType.new(value.toDouble())
    }

    override fun truth(value: Double): Boolean {
        return value != 0.0
    }

    override fun string(value: Double): String {
        return "$value rad"
    }

    override fun <B : Any> convert(from: Value<B>): Double? {
        return when (from.type) {
            NullType -> 0.0
            IntType -> from.cast(IntType)?.value?.toDouble()
            FloatType -> from.cast(FloatType)?.value
            AngleType -> from.cast(AngleType)?.value
            BoolType -> from.cast(BoolType)?.value?.let { if (it) 1.0 else 0.0 }
            StrType -> from.cast(StrType)?.value?.let { it.toDoubleOrNull() ?: Double.NaN }

            else -> null
        }
    }


    // Comparison in floats is special since ints can be compared to floats. Floats rank above ints so whenever a float
    // and an int are compared, the FloatType will always handle it.

    private fun comparableValue(value: Value<*>): Double? {
        return value.castValue(AngleType)
    }

    override fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
        val lhsCmp = comparableValue(lhs) ?: return Compare.INCOMPARABLE
        val rhsCmp = comparableValue(rhs) ?: return Compare.INCOMPARABLE
        return Compare.compare(lhsCmp, rhsCmp)
    }


    override fun plus(value: Value<*>) = value

    override fun neg(value: Value<*>) = value.map(this, this) {
        -it
    }

    override fun abs(value: Value<*>) = value.map(this, this) {
        it.absoluteValue
    }


    private fun arithmeticValue(value: Value<*>): Double? {
        return when (value.type) {
            IntType -> value.castValue(IntType)?.toDouble()
            FloatType -> value.castValue(FloatType)
            BoolType -> value.castValue(BoolType)?.let { if (it) 1.0 else 0.0 }
            else -> value.castValue(AngleType)
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
            // Multiplying angles makes solid angles, which astronomers would be proud of but we're generating vector
            // graphics here so let's not
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
            // We can divide radians by radians, it gives us a number without unit
            // Not that anyone gives a shit but let's allow it

            val lhsVal = lhs.castValue(this) ?: return null
            val rhsVal = lhs.castValue(this) ?: return null

            return FloatType.new(lhsVal / rhsVal)
        }

        if (lhs.isType(this)) {
            val lhsVal = lhs.castValue(this) ?: return null
            val rhsVal = scalarValue(rhs) ?: return null

            return new(lhsVal / rhsVal)
        } else {
            // Dividing numbers by angles creates weird units like "radians ^ -1" which we cannot represent
            return null
        }
    }
}