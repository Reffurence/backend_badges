package net.foxboi.badger.model.dyn

import net.foxboi.badger.Badger
import net.foxboi.badger.asset.Asset
import net.foxboi.badger.expr.*
import kotlin.math.*

class DynEvaluator(
    val varGetter: (String) -> Value<*>?
) : Evaluator {
    override fun getVar(name: String): Value<*>? {
        return varGetter(name)
    }

    override fun getFun(name: String) = when (name) {
        "exists" -> exists
        "e" -> e
        "pi" -> pi
        "sqrt" -> sqrt
        "cbrt" -> cbrt
        "abs" -> abs
        "sign" -> sign
        "round" -> round
        "floor" -> floor
        "ceil" -> ceil
        "ln" -> ln
        "log2" -> log2
        "log10" -> log10
        "exp" -> exp
        "sin" -> sin
        "cos" -> cos
        "tan" -> tan
        "sinh" -> sinh
        "cosh" -> cosh
        "tanh" -> tanh
        "asin" -> asin
        "acos" -> acos
        "atan" -> atan
        "asinh" -> asinh
        "acosh" -> acosh
        "atanh" -> atanh
        "atan2" -> atan2
        "log" -> log
        "pow" -> pow
        "hypot" -> hypot
        "min" -> min
        "max" -> max
        else -> null
    }

    companion object {
        private val exists = Fun { (name) ->
            val asset = Asset.fromStringOrNull(name.string) ?: return@Fun BoolType.falseValue
            BoolType.new(Badger.assets.exists(asset))
        }

        private val e = nullaryMath(E)
        private val pi = nullaryMath(PI)
        private val sqrt = unaryMath(::sqrt)
        private val cbrt = unaryMath(::cbrt)
        private val abs = unaryMath(::abs)
        private val sign = unaryMath(::sign)
        private val round = unaryMath(::round)
        private val floor = unaryMath(::floor)
        private val ceil = unaryMath(::ceil)
        private val ln = unaryMath(::ln)
        private val log2 = unaryMath(::log2)
        private val log10 = unaryMath(::log10)
        private val exp = unaryMath(::exp)
        private val sin = unaryMath(::sin)
        private val cos = unaryMath(::cos)
        private val tan = unaryMath(::tan)
        private val sinh = unaryMath(::sinh)
        private val cosh = unaryMath(::cosh)
        private val tanh = unaryMath(::tanh)
        private val asin = unaryMath(::asin)
        private val acos = unaryMath(::acos)
        private val atan = unaryMath(::atan)
        private val asinh = unaryMath(::asinh)
        private val acosh = unaryMath(::acosh)
        private val atanh = unaryMath(::atanh)
        private val atan2 = binaryMath(::atan2)
        private val log = binaryMath(::log)
        private val pow = binaryMath(Double::pow)
        private val hypot = binaryMath(::hypot)


        private fun fnError(op: String, vals: List<Value<*>>): Nothing {
            error("Function '$op' is not applicable to ${vals.joinToString(", ") { "'${it.type.name}'" }}")
        }

        private val min = Fun { args ->
            var min = NullType.nullValue as Value<*>
            var first = true

            for (v in args) {
                if (first) {
                    min = v
                    first = false
                } else {
                    val cmp = Value.compare(v, min)
                    when (cmp) {
                        Compare.LESS -> min = v
                        Compare.INCOMPARABLE -> fnError("min", args)
                        else -> Unit
                    }
                }
            }

            min
        }

        private val max = Fun { args ->
            var max = NullType.nullValue as Value<*>
            var first = true

            for (v in args) {
                if (first) {
                    max = v
                    first = false
                } else {
                    val cmp = Value.compare(v, max)
                    when (cmp) {
                        Compare.GREATER -> max = v
                        Compare.INCOMPARABLE -> fnError("max", args)
                        else -> Unit
                    }
                }
            }

            max
        }

        private fun nullaryMath(v: Double) = Fun { _ ->
            FloatType.new(v)
        }

        private inline fun unaryMath(crossinline fn: (Double) -> Double) = Fun { (v) ->
            val num = v.convertValue(FloatType) ?: return@Fun FloatType.new(Double.NaN)
            FloatType.new(fn(num))
        }

        private inline fun binaryMath(crossinline fn: (Double, Double) -> Double) = Fun { (v, w) ->
            val num1 = v.convertValue(FloatType) ?: return@Fun FloatType.new(Double.NaN)
            val num2 = w.convertValue(FloatType) ?: return@Fun FloatType.new(Double.NaN)
            FloatType.new(fn(num1, num2))
        }
    }
}