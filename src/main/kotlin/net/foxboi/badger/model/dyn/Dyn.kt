package net.foxboi.badger.model.dyn

import net.foxboi.badger.expr.*
import net.foxboi.badger.util.Color
import net.foxboi.badger.util.Length

/**
 * A dynamic, [Scope]-dependent value.
 */
sealed interface Dyn<out T> {
    /**
     * Resolves the value given a [Scope].
     */
    infix fun via(scope: Scope): T

    /**
     * Returns a dynamic value derived from this value using the given mapping function.
     */
    fun <U> map(map: (T) -> U): Dyn<U> {
        return Map(this, map)
    }

    private data class Const<T>(val value: T) : Dyn<T> {
        override fun via(scope: Scope): T {
            return value
        }
    }

    private data class Eval<T>(val expr: Expr, val unwrap: (Value<*>) -> T) : Dyn<T> {
        override fun via(scope: Scope): T {
            val value = scope.eval(expr)
            return unwrap(value)
        }
    }

    private data class Map<T, U>(val dyn: Dyn<T>, val map: (T) -> U) : Dyn<U> {
        override fun via(scope: Scope): U {
            return map(dyn.via(scope))
        }
    }

    companion object {
        /**
         * Returns a [Dyn] value that is independent of scope and always returns the given value.
         */
        fun <T> const(value: T): Dyn<T> {
            return Const(value)
        }

        /**
         * Returns a [Dyn] value evaluates the given expression and unwraps the resulting [Value] using the given
         * unwrap function.
         */
        fun <T> eval(expr: Expr, unwrap: (Value<*>) -> T): Dyn<T> {
            return Eval(expr, unwrap)
        }

        /**
         * Returns a [Dyn] value evaluates the given expression and unwraps the resulting [Value] by converting it to
         * the specified [Type], if possible. If the conversion fails, a [DynException] is thrown.
         */
        fun <T : Any> eval(expr: Expr, type: Type<T>): Dyn<T> {
            return eval(expr) {
                val value = it.convert(type) ?: throw DynException("Can't convert ${it.type.name} to ${type.name}")
                value.value
            }
        }

        /**
         * Returns a [Dyn] value evaluates the given expression and unwraps the resulting [Value] by converting it to
         * the specified [Type], if possible, and to `null` if the given value is of the [NullType].
         * If the conversion fails, a [DynException] is thrown.
         */
        fun <T : Any> optEval(expr: Expr, type: Type<T>): Dyn<T?> {
            return eval(expr) {
                if (it.isType(NullType)) {
                    null
                } else {
                    val value = it.convert(type) ?: throw DynException("Can't convert ${it.type.name} to ${type.name}")
                    value.value
                }
            }
        }

        fun string(expr: Expr): Dyn<String> {
            return eval(expr, StrType)
        }

        fun optString(expr: Expr): Dyn<String?> {
            return optEval(expr, StrType)
        }

        fun boolean(expr: Expr): Dyn<Boolean> {
            return eval(expr, BoolType)
        }

        fun optBoolean(expr: Expr): Dyn<Boolean?> {
            return optEval(expr, BoolType)
        }

        fun double(expr: Expr): Dyn<Double> {
            return eval(expr, FloatType)
        }

        fun optDouble(expr: Expr): Dyn<Double?> {
            return optEval(expr, FloatType)
        }

        fun float(expr: Expr): Dyn<Float> {
            return eval(expr, FloatType).map { it.toFloat() }
        }

        fun optFloat(expr: Expr): Dyn<Float?> {
            return optEval(expr, FloatType).map { it?.toFloat() }
        }

        fun long(expr: Expr): Dyn<Long> {
            return eval(expr, IntType)
        }

        fun optLong(expr: Expr): Dyn<Long?> {
            return optEval(expr, IntType)
        }

        fun int(expr: Expr): Dyn<Int> {
            return eval(expr, IntType).map { it.toInt() }
        }

        fun optInt(expr: Expr): Dyn<Int?> {
            return optEval(expr, IntType).map { it?.toInt() }
        }

        fun length(expr: Expr): Dyn<Length> {
            return eval(expr, LenType)
        }

        fun optLength(expr: Expr): Dyn<Length?> {
            return optEval(expr, LenType)
        }

        fun col(expr: Expr): Dyn<Color> {
            return eval(expr, ColType)
        }

        fun optCol(expr: Expr): Dyn<Color?> {
            return optEval(expr, ColType)
        }

        fun angle(expr: Expr): Dyn<Double> {
            return eval(expr, AngleType)
        }

        fun optAngle(expr: Expr): Dyn<Double?> {
            return optEval(expr, AngleType)
        }
    }
}