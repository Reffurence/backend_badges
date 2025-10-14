package net.foxboi.badger.expr

import net.foxboi.badger.parser.ExprLexer
import net.foxboi.badger.parser.ExprParser
import net.foxboi.badger.parser.ExprVisitor
import net.foxboi.badger.util.Color
import net.foxboi.badger.util.Length
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.math.PI

//
// Expression parsing is done by ANTLR. You can find the grammar in the 'parser' subproject.
// This code is in a subproject to keep Java code out of the main project.
//

fun parseExpr(input: String): Expr {
    val lexer = ExprLexer(ANTLRInputStream(input))
    lexer.errorListeners.clear()
    lexer.addErrorListener(ExprErrorListener)

    val parser = ExprParser(BufferedTokenStream(lexer))
    parser.errorListeners.clear()
    parser.addErrorListener(ExprErrorListener)

    val tree = parser.input()
    return tree.accept(ExprGenerator)
}

private object ValueParsers {
    private fun error(rule: Token, message: String): Nothing {
        throw ExpressionSyntaxException("${rule.line}:${rule.charPositionInLine}: $message")
    }

    fun parseFloat(node: Token): Double {
        val text = node.text
        return text.toDoubleOrNull() ?: error(node, "Malformed number '$text'")
    }

    fun parseInt(node: Token): Long {
        val text = node.text
        return text.toLongOrNull() ?: error(node, "Malformed number '$text'")
    }

    fun parseColor(node: Token): Color {
        val text = node.text
        return Color.fromHexOrNull(text) ?: error(node, "Malformed number '$text'")
    }

    fun parseUEscape(esc: String): Char? {
        return esc.toIntOrNull(16)?.toChar()
    }

    fun parseStringContent(content: String): String? {
        val builder = StringBuilder()

        var i = 0
        val l = content.length

        while (i < l) {
            val ch = content[i++]

            if (ch == '\\') {
                if (i >= l) return null

                builder.append(
                    when (val esc = content[i++]) {
                        '\'', '"', '\\' -> esc
                        'n' -> '\n'
                        'r' -> '\r'
                        'b' -> '\b'
                        't' -> '\t'
                        'u' -> {
                            if (i + 4 > content.length) return null
                            i += 4
                            parseUEscape(content.substring(i - 4, i)) ?: return null
                        }

                        else -> return null
                    }
                )
            } else {
                builder.append(ch)
            }
        }

        return builder.toString()
    }

    fun parseString(node: Token): String {
        val text = node.text
        if (text.isEmpty() || text.first() != text.last()) error(node, "Malformed string $text")
        if (text.first() != '\'' && text.first() != '"') error(node, "Malformed string $text")
        return parseStringContent(text.substring(1, text.length - 1)) ?: error(node, "Malformed string $text")
    }
}

private object ExprGenerator : ExprVisitor<Expr> {
    override fun visit(p0: ParseTree): Expr {
        throw ExpressionSyntaxException("Unrecognised syntax error")
    }

    override fun visitChildren(rule: RuleNode): Expr {
        throw ExpressionSyntaxException("Unrecognised syntax error")
    }

    override fun visitTerminal(p0: TerminalNode): Expr {
        throw ExpressionSyntaxException("Unrecognised syntax error")
    }

    override fun visitErrorNode(p0: ErrorNode): Expr {
        throw ExpressionSyntaxException("Unrecognised syntax error")
    }

    private fun invalidUnit(rule: TerminalNode): Nothing {
        throw ExpressionSyntaxException("${rule.symbol.line}:${rule.symbol.charPositionInLine}: Invalid unit '${rule.text}'")
    }

    override fun visitUnitNumber(ctx: ExprParser.UnitNumberContext): Expr {
        val num = ctx.v.text.toDouble()
        val unit = ctx.IDENT().text

        return when (unit) {
            "px" -> Expr.const(LenType.new(Length.pixels(num)))
            "in" -> Expr.const(LenType.new(Length.inches(num)))
            "ft" -> Expr.const(LenType.new(Length.feet(num)))
            "yd" -> Expr.const(LenType.new(Length.yards(num)))
            "mm" -> Expr.const(LenType.new(Length.millimetres(num)))
            "cm" -> Expr.const(LenType.new(Length.centimetres(num)))
            "m" -> Expr.const(LenType.new(Length.metres(num)))
            "pt" -> Expr.const(LenType.new(Length.points(num)))
            "pc" -> Expr.const(LenType.new(Length.pica(num)))
            "self" -> Expr.const(LenType.new(Length.selfPerc(num)))
            "par" -> Expr.const(LenType.new(Length.parentPerc(num)))
            "deg" -> Expr.const(AngleType.new(num / 180 * PI))
            "rad" -> Expr.const(AngleType.new(num))
            else -> invalidUnit(ctx.IDENT())
        }
    }

    override fun visitPercentNumber(ctx: ExprParser.PercentNumberContext): Expr {
        return Expr.const(LenType.new(Length.parentPerc(ValueParsers.parseFloat(ctx.v))))
    }

    override fun visitPureNumber(ctx: ExprParser.PureNumberContext): Expr {
        return Expr.const(FloatType.new(ValueParsers.parseFloat(ctx.FLOAT().symbol)))
    }

    override fun visitStrExpr(ctx: ExprParser.StrExprContext): Expr {
        return Expr.const(StrType.new(ValueParsers.parseString(ctx.str)))
    }

    override fun visitIntExpr(ctx: ExprParser.IntExprContext): Expr {
        return Expr.const(IntType.new(ValueParsers.parseInt(ctx.INT().symbol)))
    }

    override fun visitNumExpr(ctx: ExprParser.NumExprContext): Expr {
        return ctx.number().accept(this)
    }

    override fun visitColExpr(ctx: ExprParser.ColExprContext): Expr {
        return Expr.const(ColType.new(ValueParsers.parseColor(ctx.col)))
    }


    override fun visitVarExpr(ctx: ExprParser.VarExprContext): Expr {
        return Expr.name(ctx.`var`.text.substring(1))
    }


    override fun visitParExpr(ctx: ExprParser.ParExprContext): Expr {
        return ctx.expr().accept(this)
    }


    private fun binary(lhs: Expr, rhs: Expr, op: Int): Expr {
        val builder = ExprBuilder(lhs.ops.size + rhs.ops.size + 1)
        builder.append(lhs)
        builder.append(rhs)
        builder.opcode(op)
        return builder.build()
    }

    override fun visitLogAndExpr(ctx: ExprParser.LogAndExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.LAND)
    }

    override fun visitLogOrExpr(ctx: ExprParser.LogOrExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.LOR)
    }

    override fun visitLogXorExpr(ctx: ExprParser.LogXorExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.LXOR)
    }


    override fun visitBitAndExpr(ctx: ExprParser.BitAndExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.BAND)
    }

    override fun visitBitOrExpr(ctx: ExprParser.BitOrExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.BOR)
    }

    override fun visitBitXorExpr(ctx: ExprParser.BitXorExprContext): Expr {
        return binary(ctx.lhs.accept(this), ctx.rhs.accept(this), Ops.BXOR)
    }

    override fun visitShiftExpr(ctx: ExprParser.ShiftExprContext): Expr {
        return binary(
            ctx.lhs.accept(this), ctx.rhs.accept(this), when (ctx.op.text) {
                "<<" -> Ops.SHL
                ">>" -> Ops.SHR
                else -> throw AssertionError()
            }
        )
    }


    override fun visitCondExpr(ctx: ExprParser.CondExprContext): Expr {
        val cond = ctx.cond.accept(this)
        val yes = ctx.yes.accept(this)
        val no = ctx.no.accept(this)

        val builder = ExprBuilder(cond.ops.size + yes.ops.size + no.ops.size + 1)
        builder.append(cond)
        builder.append(yes)
        builder.append(no)
        builder.opcode(Ops.COND)
        return builder.build()
    }

    override fun visitCmpExpr(ctx: ExprParser.CmpExprContext): Expr {
        return binary(
            ctx.lhs.accept(this), ctx.rhs.accept(this), when (ctx.op.text) {
                "<" -> Ops.LT
                ">" -> Ops.GT
                "<=" -> Ops.LE
                ">=" -> Ops.GE
                else -> throw AssertionError()
            }
        )
    }

    override fun visitEqExpr(ctx: ExprParser.EqExprContext): Expr {
        return binary(
            ctx.lhs.accept(this), ctx.rhs.accept(this), when (ctx.op.text) {
                "==" -> Ops.EQ
                "!=" -> Ops.NEQ
                "===" -> Ops.SAME
                "!==" -> Ops.NSAME
                else -> throw AssertionError()
            }
        )
    }


    override fun visitAddExpr(ctx: ExprParser.AddExprContext): Expr {
        return binary(
            ctx.lhs.accept(this), ctx.rhs.accept(this), when (ctx.op.text) {
                "+" -> Ops.ADD
                "-" -> Ops.SUB
                else -> throw AssertionError()
            }
        )
    }

    override fun visitMulExpr(ctx: ExprParser.MulExprContext): Expr {
        return binary(
            ctx.lhs.accept(this), ctx.rhs.accept(this), when (ctx.op.text) {
                "*" -> Ops.MUL
                "/" -> Ops.DIV
                "%" -> Ops.REM
                else -> throw AssertionError()
            }
        )
    }


    override fun visitUnaryExpr(ctx: ExprParser.UnaryExprContext): Expr {
        val rhs = ctx.rhs.accept(this)

        val builder = ExprBuilder(rhs.ops.size + 1)
        builder.append(rhs)
        builder.opcode(
            when (ctx.op.text) {
                "+" -> Ops.PLUS
                "-" -> Ops.NEG
                "!" -> Ops.LNOT
                "~" -> Ops.BNOT
                else -> throw AssertionError()
            }
        )
        return builder.build()
    }

    override fun visitCallExpr(ctx: ExprParser.CallExprContext): Expr {
        val name = ctx.name.text
        val args = ctx.args.map { it.accept(this) }
        val ops = args.sumOf { it.ops.size }


        val builder = ExprBuilder(ops + 3)
        for (arg in args.asReversed()) { // Put in reverse so they pop from stack in the right order
            builder.append(arg)
        }
        builder.opcode(Ops.CALL)
        builder.name(name)
        builder.opcode(args.size)

        return builder.build()
    }

    override fun visitConstExpr(ctx: ExprParser.ConstExprContext): Expr {
        return when (ctx.`val`.text) {
            "null" -> Expr.constNull()
            "true" -> Expr.const(BoolType.trueValue)
            "false" -> Expr.const(BoolType.falseValue)
            "nan" -> Expr.const(FloatType.new(Double.NaN))
            "inf" -> Expr.const(FloatType.new(Double.POSITIVE_INFINITY))
            else -> throw AssertionError()
        }
    }

    override fun visitInput(ctx: ExprParser.InputContext): Expr {
        return ctx.expr().accept(this)
    }

}

private object ExprErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException
    ) {
        throw ExpressionSyntaxException("$line:$charPositionInLine: $msg")
    }
}