package net.foxboi.badger.expr

import kotlin.math.absoluteValue

data object IntType : Type<Long> {
    override val rank = TypeRank.INT
    override val name = "int"

    fun new(value: Int): Value<Long> {
        return new(value.toLong())
    }

    override fun truth(value: Long): Boolean {
        return value != 0L
    }

    override fun string(value: Long): String {
        return "$value"
    }

    override fun <B : Any> convert(from: Value<B>): Long? {
        return when (from.type) {
            NullType -> 0L
            IntType -> from.cast(IntType)?.value
            FloatType -> from.cast(FloatType)?.value?.toLong()
            AngleType -> from.cast(AngleType)?.value?.toLong()
            BoolType -> from.cast(BoolType)?.value?.let { if (it) 1L else 0L }
            StrType -> from.cast(StrType)?.value?.let { it.toLongOrNull() ?: 0L }

            else -> null
        }
    }

    override fun compare(lhs: Value<*>, rhs: Value<*>): Compare {
        // Floats rank higher so FloatType will take care of comparing ints and floats
        return Compare.compare(this, lhs, rhs)
    }


    override fun plus(value: Value<*>) = value

    override fun neg(value: Value<*>) = value.map(this, this) {
        -it
    }

    override fun abs(value: Value<*>) = value.map(this, this) {
        it.absoluteValue
    }

    override fun bitNot(value: Value<*>) = value.map(this, this) {
        it.inv()
    }


    private fun arithmeticValue(value: Value<*>): Long? {
        return when (value.type) {
            BoolType -> value.castValue(BoolType)?.let { if (it) 1L else 0L }
            else -> value.castValue(IntType)
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
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue * rhsValue)
    }

    override fun div(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue / rhsValue)
    }

    override fun rem(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue % rhsValue)
    }


    override fun bitAnd(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue and rhsValue)
    }

    override fun bitOr(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue or rhsValue)
    }

    override fun bitXor(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null
        return new(lhsValue xor rhsValue)
    }

    override fun leftSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null

        if (rhsValue > 65 || rhsValue < -65) {
            return new(0) // Shift overflow; manual catch since converting long to int may result in a high value becoming a small value
        }

        return new(lhsValue shl rhsValue.toInt())
    }

    override fun rightSh(lhs: Value<*>, rhs: Value<*>): Value<*>? {
        val lhsValue = arithmeticValue(lhs) ?: return null
        val rhsValue = arithmeticValue(rhs) ?: return null

        if (rhsValue > 65 || rhsValue < -65) {
            return new(0) // Shift overflow; manual catch since converting long to int may result in a high value becoming a small value
        }

        return new(lhsValue ushr rhsValue.toInt())
    }
}