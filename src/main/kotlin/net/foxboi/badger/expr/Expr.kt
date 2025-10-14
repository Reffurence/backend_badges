package net.foxboi.badger.expr

import java.util.*

object Ops {
    const val NULL = 0
    const val BOOL = 1
    const val INT = 2
    const val FLOAT = 3
    const val STR = 4
    const val LEN = 5
    const val ANGLE = 6
    const val COL = 7

    val types = arrayOf(
        NullType,
        BoolType,
        IntType,
        FloatType,
        StrType,
        LenType,
        AngleType,
        ColType,
    )

    val typeToId = mapOf(
        NullType to NULL,
        BoolType to BOOL,
        IntType to INT,
        FloatType to FLOAT,
        StrType to STR,
        LenType to LEN,
        AngleType to ANGLE,
        ColType to COL
    )


    const val NOOP = 0x0
    const val DUP = 0x1
    const val POP = 0x2
    const val SWAP = 0x3

    const val PLUS = 0x10
    const val NEG = 0x11
    const val ABS = 0x12
    const val ADD = 0x13
    const val SUB = 0x14
    const val MUL = 0x15
    const val DIV = 0x16
    const val REM = 0x17

    const val SHL = 0x20
    const val SHR = 0x21
    const val BAND = 0x22
    const val BOR = 0x23
    const val BXOR = 0x24
    const val BNOT = 0x25

    const val LAND = 0x30
    const val LOR = 0x31
    const val LXOR = 0x32
    const val LNOT = 0x33

    const val LT = 0x40
    const val GT = 0x41
    const val LE = 0x42
    const val GE = 0x43
    const val EQ = 0x44
    const val NEQ = 0x45
    const val SAME = 0x46
    const val NSAME = 0x47

    const val LDNULL = 0x50
    const val LDC = 0x51
    const val LDV = 0x52

    const val CAST = 0x60
    const val CONV = 0x61

    const val COND = 0x70

    const val CALL = 0x80

    const val ASSET_LOCATE = 0x90
}

private fun error(msg: String): Nothing {
    throw ExpressionEvalException(msg)
}

private fun opError(op: String, vararg vals: Value<*>): Nothing {
    error("Operator '$op' is not applicable to ${vals.joinToString(", ") { "'${it.type.name}'" }}")
}

/**
 * An unevaluated expression.
 */
class Expr internal constructor(
    internal val ops: IntArray,
    internal val consts: List<Value<*>>,
    internal val names: List<String>
) {
    fun eval(varGetter: (String) -> Value<*>?): Value<*> {
        return eval(object : Evaluator {
            override fun getVar(name: String): Value<*>? {
                return varGetter(name)
            }

            override fun getFun(name: String): Fun? {
                return null
            }
        })
    }

    fun eval(ctx: Evaluator): Value<*> {
        val stack = Stack<Value<*>>()

        val args = mutableListOf<Value<*>>()

        visit(ops, consts, names) { op, const, name, type, params ->
            when (op) {
                Ops.NOOP -> {}
                Ops.DUP -> {
                    val e = stack.pop()
                    stack.push(e)
                    stack.push(e)
                }

                Ops.POP -> {
                    stack.pop()
                }

                Ops.SWAP -> {
                    val e1 = stack.pop()
                    val e2 = stack.pop()
                    stack.push(e1)
                    stack.push(e2)
                }

                Ops.PLUS -> {
                    val rhs = stack.pop()
                    stack.push(Value.plus(rhs) ?: opError("+", rhs))
                }

                Ops.NEG -> {
                    val rhs = stack.pop()
                    stack.push(Value.neg(rhs) ?: opError("-", rhs))
                }

                Ops.ABS -> {
                    val rhs = stack.pop()
                    stack.push(Value.abs(rhs) ?: opError("|...|", rhs))
                }

                Ops.ADD -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.add(lhs, rhs) ?: opError("+", lhs, rhs))
                }

                Ops.SUB -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.sub(lhs, rhs) ?: opError("-", lhs, rhs))
                }

                Ops.MUL -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.mul(lhs, rhs) ?: opError("*", lhs, rhs))
                }

                Ops.DIV -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.div(lhs, rhs) ?: opError("/", lhs, rhs))
                }

                Ops.REM -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.rem(lhs, rhs) ?: opError("%", lhs, rhs))
                }

                Ops.SHL -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.leftSh(lhs, rhs) ?: opError("<<", lhs, rhs))
                }

                Ops.SHR -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.rightSh(lhs, rhs) ?: opError(">>", lhs, rhs))
                }

                Ops.BAND -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.bitAnd(lhs, rhs) ?: opError("&", lhs, rhs))
                }

                Ops.BOR -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.bitOr(lhs, rhs) ?: opError("|", lhs, rhs))
                }

                Ops.BXOR -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.bitXor(lhs, rhs) ?: opError("^", lhs, rhs))
                }

                Ops.BNOT -> {
                    val rhs = stack.pop()
                    stack.push(Value.bitNot(rhs) ?: opError("!", rhs))
                }

                Ops.LAND -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.logicAnd(lhs, rhs))
                }

                Ops.LOR -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.logicOr(lhs, rhs))
                }

                Ops.LXOR -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.logicXor(lhs, rhs))
                }

                Ops.LNOT -> {
                    val rhs = stack.pop()
                    stack.push(Value.logicNot(rhs))
                }

                Ops.LT -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.lt(lhs, rhs) ?: opError("<", rhs))
                }

                Ops.GT -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.gt(lhs, rhs) ?: opError(">", rhs))
                }

                Ops.LE -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.le(lhs, rhs) ?: opError("<=", rhs))
                }

                Ops.GE -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.ge(lhs, rhs) ?: opError(">=", rhs))
                }

                Ops.EQ -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.eq(lhs, rhs))
                }

                Ops.NEQ -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.neq(lhs, rhs))
                }

                Ops.SAME -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.same(lhs, rhs))
                }

                Ops.NSAME -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    stack.push(Value.notSame(lhs, rhs))
                }

                Ops.LDNULL -> {
                    stack.push(NullType.nullValue)
                }

                Ops.LDC -> {
                    stack.push(const)
                }

                Ops.LDV -> {
                    stack.push(ctx.getVar(name!!) ?: error("Unbound variable $$name"))
                }

                Ops.CAST -> {
                    val rhs = stack.pop()
                    stack.push(rhs.cast(type!!) ?: error("Can't cast ${rhs.type.name} to ${type.name}"))
                }

                Ops.CONV -> {
                    val rhs = stack.pop()
                    stack.push(rhs.convert(type!!) ?: error("Can't convert ${rhs.type.name} to ${type.name}"))
                }

                Ops.COND -> {
                    val rhs = stack.pop()
                    val lhs = stack.pop()
                    val cond = stack.pop()

                    stack.push(if (cond.truth) lhs else rhs)
                }

                Ops.CALL -> {
                    args.clear()

                    var i = 0
                    while (i < params) {
                        args += stack.pop()
                        i++
                    }

                    val f = ctx.getFun(name!!) ?: error("Unknown function $name")
                    stack.push(f.call(args))
                }

                else -> error("Invalid opcode 0x%02X".format(op))
            }
        }

        if (stack.size != 1) {
            error("Expression incomplete, stack should end with one value")
        }

        return stack.pop()
    }

    fun writeDebug(out: (String) -> Unit) {
        visit(ops, consts, names) { op, const, name, type, params ->
            out(
                when (op) {
                    Ops.NOOP -> "NOOP"
                    Ops.DUP -> "DUP"
                    Ops.POP -> "POP"
                    Ops.SWAP -> "SWAP"
                    Ops.PLUS -> "PLUS"
                    Ops.NEG -> "NEG"
                    Ops.ABS -> "ABS"
                    Ops.ADD -> "ADD"
                    Ops.SUB -> "SUB"
                    Ops.MUL -> "MUL"
                    Ops.DIV -> "DIV"
                    Ops.REM -> "REM"
                    Ops.SHL -> "SHL"
                    Ops.SHR -> "SHR"
                    Ops.BAND -> "BAND"
                    Ops.BOR -> "BOR"
                    Ops.BXOR -> "BXOR"
                    Ops.BNOT -> "BNOT"
                    Ops.LAND -> "LAND"
                    Ops.LOR -> "LOR"
                    Ops.LXOR -> "LXOR"
                    Ops.LNOT -> "LNOT"
                    Ops.LT -> "LT"
                    Ops.GT -> "GT"
                    Ops.LE -> "LE"
                    Ops.GE -> "GE"
                    Ops.EQ -> "EQ"
                    Ops.NEQ -> "NEQ"
                    Ops.SAME -> "SAME"
                    Ops.NSAME -> "NSAME"
                    Ops.LDNULL -> "LDNULL"
                    Ops.LDC -> "LDC ${const?.string}"
                    Ops.LDV -> "LDV $$name"
                    Ops.CAST -> "CAST ${type?.name}"
                    Ops.CONV -> "CONV ${type?.name}"
                    Ops.COND -> "COND"
                    Ops.CALL -> "CALL $name#$params"
                    Ops.ASSET_LOCATE -> "ASSET_LOCATE"

                    else -> "[invalid]"
                }
            )
        }
    }

    fun thenConvertTo(type: Type<*>): Expr {
        val builder = ExprBuilder(ops.size + 2)
        builder.append(this)
        builder.opcode(Ops.CONV)
        builder.type(type)
        return builder.build()
    }

    companion object {
        fun constNull(): Expr {
            return Expr(
                intArrayOf(Ops.LDNULL),
                listOf(),
                listOf()
            )
        }

        fun const(value: Value<*>): Expr {
            return Expr(
                intArrayOf(Ops.LDC, 0),
                listOf(value),
                listOf()
            )
        }

        fun name(name: String): Expr {
            return Expr(
                intArrayOf(Ops.LDV, 0),
                listOf(),
                listOf(name)
            )
        }
    }
}

private inline fun visit(
    ops: IntArray,
    consts: List<Value<*>>,
    names: List<String>,
    code: (op: Int, const: Value<*>?, name: String?, type: Type<*>?, params: Int) -> Unit
) {
    val len = ops.size

    var i = 0

    while (i < len) when (val op = ops[i]) {
        Ops.LDC -> {
            code(op, consts[ops[i + 1]], null, null, -1)
            i += 2
        }

        Ops.LDV -> {
            code(op, null, names[ops[i + 1]], null, -1)
            i += 2
        }

        Ops.CAST, Ops.CONV -> {
            code(op, null, null, Ops.types[ops[i + 1]], -1)
            i += 2
        }

        Ops.CALL -> {
            code(op, null, names[ops[i + 1]], null, ops[i + 2])
            i += 3
        }

        else -> {
            code(op, null, null, null, -1)
            i++
        }
    }
}


class ExprBuilder(capacity: Int) {
    private var ops = IntArray(capacity)
    private var index = 0

    private val consts = mutableListOf<Value<*>>()
    private val names = mutableListOf<String>()

    private var constIndices = mutableMapOf<Value<*>, Int>()
    private var nameIndices = mutableMapOf<String, Int>()

    fun opcode(code: Int) {
        if (index >= ops.size) {
            ops = ops.copyOf(ops.size * 2)
        }
        ops[index++] = code
    }

    fun const(value: Value<*>) {
        val index = constIndices.getOrPut(value) {
            val len = consts.size
            consts += value
            len
        }
        opcode(index)
    }

    fun name(name: String) {
        val index = nameIndices.getOrPut(name) {
            val len = names.size
            names += name
            len
        }
        opcode(index)
    }

    fun type(name: Type<*>) {
        val index = Ops.typeToId[name] ?: 0
        opcode(index)
    }

    fun append(expr: Expr) {
        visit(expr.ops, expr.consts, expr.names) { op, const, name, type, params ->
            opcode(op)
            if (const != null) const(const)
            if (name != null) name(name)
            if (type != null) type(type)
            if (params >= 0) opcode(params)
        }
    }

    fun append(expr: ExprBuilder) {
        visit(expr.ops, expr.consts, expr.names) { op, const, name, type, params ->
            opcode(op)
            if (const != null) const(const)
            if (name != null) name(name)
            if (type != null) type(type)
            if (params >= 0) opcode(params)
        }
    }

    fun build() = Expr(ops.copyOf(index), consts.toList(), names.toList())

    fun reset() {
        index = 0
    }
}