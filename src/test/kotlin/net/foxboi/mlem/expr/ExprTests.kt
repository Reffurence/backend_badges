package net.foxboi.mlem.expr

import kotlin.test.Test

class ExprTests {
    @Test
    fun testParse() {
        val expr = parseExpr("if 5 > 3 then 5 else 3")

        expr.writeDebug { println(it) }
    }

    @Test
    fun testEval() {
        val expr = parseExpr("\$foo + 'dd'")

        println(expr.eval { if (it == "foo") IntType.new(4) else null })
    }
}