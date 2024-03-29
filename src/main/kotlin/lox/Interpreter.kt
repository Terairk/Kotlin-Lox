package org.terairk.lox

import org.terairk.lox.TokenType.*

// Any? is probably the closest thing to Object in java
class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals: Environment = Environment()
    private var environment: Environment = globals
    private val locals = HashMap<Expr, Int>()

    init {
        globals.define("clock", object: LoxCallable {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return (System.currentTimeMillis() / 1000.0)
            }

            override fun toString(): String {
                return "<native fn>"
            }

        })
    }

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

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right: Any? = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                (right as Double).unaryMinus()
            }
            BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals[name]
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
            STAR -> {
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

                throw RuntimeError(
                    expr.operator,
                    "Operands must be two numbers or two strings.",
                )
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

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments: MutableList<Any?> = mutableListOf()

        expr.arguments.forEach { arguments.add(evaluate(it)) }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren,
                "Can only call functions and classes.")
        }

        val function: LoxCallable = callee

        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren,
                "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val instance = evaluate(expr.instance)
        if (instance is LoxInstance) {
            return instance[expr.name]
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val instance = evaluate(expr.instance)
        if (instance !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        instance[expr.name] = value
        return value
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance = locals[expr]

        if (distance != null) {
            val superclass = environment.getAt(distance, "super") as LoxClass
            val instance = environment.getAt(distance - 1, "this") as LoxInstance
            val method = superclass.findMethod(expr.method.lexeme)
                ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}.")

            return method.bind(instance)
        }

        // shouldn't get here
        return null
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

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    // this two stage binding process allows references to the class inside its own methods
    override fun visitClassStmt(stmt: Stmt.Class) {
        var superclass: Any? = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }
        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods[method.name.lexeme] = function
        }

        val klass = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)
        if (superclass != null) {
            environment = environment.enclosing!!
        }
        environment.assign(stmt.name, klass)
    }

    fun executeBlock(
        statements: List<Stmt>,
        environment: Environment,
    ) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        var value: Any? = null
        if (stmt.value != null) value = evaluate(stmt.value)

        // basically Return is an Exception class, when we throw this
        // we'll unwind the call stack until a suitable exception handler is found
        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    private fun isTruthy(any: Any?): Boolean {
        if (any == null) return false
        if (any is Boolean) return any as Boolean
        return true
    }

    private fun checkNumberOperand(
        operator: Token,
        operand: Any?,
    ) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(
        operator: Token,
        left: Any?,
        right: Any?,
    ) {
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
