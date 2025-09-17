package net.foxboi.mlem.expr

import net.foxboi.mlem.util.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueTests {
    @Test
    fun test1() {
        val a = FloatType.new(42)
        val b = IntType.new(42)

        assertEquals(FloatType.new(42 * 42), Value.mul(a, b))
    }

    @Test
    fun test2() {
        val a = ColType.new(Color(23, 23, 81, 88))
        val b = IntType.new(42)

        assertEquals(null, Value.mul(a, b))
    }
}