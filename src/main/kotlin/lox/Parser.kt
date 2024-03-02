package org.terairk.lox

import org.terairk.lox.TokenType.*
import kotlin.RuntimeException

class Parser(val tokens: List<Token>) {
    private var current: Int = 0

    // methods in kotlin are public and final by default
    // this is the overall only public method and which starts things off

    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = mutableListOf()
        while (!isAtEnd()) {
            val declare = declaration()
            if (declare != null) {
                statements.add(declare)
            }
        }

        return statements
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = equality()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()
        while (match(OR)) {
            val ops = previous()
            val right = and()
            expr = Expr.Logical(expr, ops, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val ops = previous()
            val right = equality()
            expr = Expr.Logical(expr, ops, right)
        }

        return expr
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()
            if (match(FUN)) return function("function")

            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Stmt {
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(FOR)) return forStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())

        return expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for.'.")
        var initializer: Stmt?
        if (match(SEMICOLON)) {
            initializer = null
        } else if (match(VAR)) {
            initializer = varDeclaration()
        } else {
            initializer = expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = expression()
        }

        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }

        consume(RIGHT_PAREN, "Expect ')' after for clauses.")
        var body = statement()

        if (increment != null) {
            body = Stmt.Block(
                listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }
        return body

    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)

    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun varDeclaration(): Stmt {
        val name: Token = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()

        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }

                parameters.add(
                    consume(IDENTIFIER, "Expect parameter name.")
                )
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = mutableListOf()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            val decl = declaration()
            if (decl != null) {
                statements.add(decl)
            }
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    // Start of parsing expressions on their own

    private fun equality(): Expr {
        return expressionGenerator(::comparison, BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expr {
        return expressionGenerator(::term, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)
    }

    private fun term(): Expr {
        return expressionGenerator(::factor, MINUS, PLUS)
    }

    private fun factor(): Expr {
        return expressionGenerator(::unary, SLASH, STAR)
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator: Token = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = mutableListOf()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN,
            "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun primary(): Expr {
        return when {
            match(FALSE) -> Expr.Literal(false)
            match(TRUE) -> Expr.Literal(true)
            match(NIL) -> Expr.Literal(null)
            match(NUMBER, STRING) -> Expr.Literal(previous().literal)
            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Expr.Grouping(expr)
            }
            match(IDENTIFIER) -> {
                Expr.Variable(previous())
            }
            else -> throw errorParser(peek(), "Expect expression.")
        }
    }

    // Below lie the utility methods

    private fun expressionGenerator(
        term: () -> Expr,
        vararg types: TokenType,
    ): Expr {
        var expr: Expr = term()

        while (match(*types)) {
            val operator: Token = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    // tries to match the token and advances on a successful match
    private fun match(vararg types: TokenType): Boolean {
        for (type: TokenType in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(
        type: TokenType,
        message: String,
    ): Token {
        if (check(type)) return advance()
        throw errorParser(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false

        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun errorParser(
        token: Token,
        message: String,
    ): ParseError {
        error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        // advance to next token as the current token is invalid
        advance()

        // basically exits this synchronization phase
        // once semicolon or one of the keywords are reached

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {
                    // erroneous state so i'll just do nothing for now
                }
            }

            advance()
        }
    }

    // companion object wouldn't work here as you can only
    // create one companion object per class whereas we want to create
    // instances of a class, I really wanted to use companion objects here
    // to learn something new but thats not possible

    private class ParseError : RuntimeException()
}
