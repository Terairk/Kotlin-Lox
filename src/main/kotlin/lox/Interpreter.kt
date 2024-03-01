package org.terairk.lox

import org.terairk.lox.RuntimeError
import org.terairk.lox.TokenType.*

// Any? is probably the closest thing to Object in java
class Interpreter: Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { execute(it) }
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right: Any? = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                (right as Double).unaryMinus()
            }
            BANG  -> !isTruthy(right)
            else  -> null
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            STAR  -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            PLUS -> {
                if ((left is Double) && (right is Double)) {
                    // smart casting doing work
                    return left + right
                }

                if ((left is String) && (right is String)) {
                    return left + right
                }

                throw RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings.")
            }
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            BANG_EQUAL -> left != right
            EQUAL_EQUAL -> left == right
            else -> {
                // unreachable
                null
            }
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    private fun isTruthy(any: Any?): Boolean {
        if (any == null) return false
        if (any is Boolean) return any as Boolean
        return true
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(thing: Any?): String {
        if (thing == null) return "nil"

        if (thing is Double) {
            var text = thing.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }

            return text
        }

        return thing.toString()

    }

    // technically Kotlin supports == on Any?,
    //  tested this in playground and a == b would work here (without the null checks)
    // but I'm keeping these here in case this needs updating
//    private fun isEqual(a: Any?, b: Any?): Boolean {
//        if (a == null && b == null) return true
//        if (a == null) return false
//
//        return a == b
//
//    }

}
