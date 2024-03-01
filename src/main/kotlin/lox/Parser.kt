package org.terairk.lox

import org.terairk.lox.Expr
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
        return equality()
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()

            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Stmt {
        if (match(PRINT)) return printStatement()

        return expressionStatement()
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

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // Start of parsing expressions on their own

    private fun equality(): Expr {
        return expressionGenerator(::comparison, BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expr {
        return expressionGenerator(::term, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, )
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

        return primary()
    }

    private fun primary(): Expr {
        return when {
            match(FALSE) -> Expr.Literal(false)
            match(TRUE)  -> Expr.Literal(true)
            match(NIL)   -> Expr.Literal(null)
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

    private fun expressionGenerator(term : () -> Expr, vararg types: TokenType): Expr {
        var expr: Expr = term()

        while (match(*types)) {
            val operator: Token = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type: TokenType in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
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

    private fun errorParser(token: Token, message: String): ParseError {
        error(token, message)
        return ParseError()

    }

    private fun synchronize() {
        // advance to next token as the current token is invalid
        advance()

        // basically exits this synchronization phase
        // once semicolon or one of the keywords are reached

        while (!isAtEnd()) {
            if (previous().type== SEMICOLON) return

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

    private class ParseError: RuntimeException() {}

}