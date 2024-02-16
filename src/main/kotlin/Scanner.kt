package org.terairk.lox

import org.terairk.lox.TokenType.*

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1
    private var nesting = 0

    private val keywords = hashMapOf(
        "and" to AND,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE,
    )

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c: Char = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> {
                addToken(if (match('=')) BANG_EQUAL else BANG)
            }
            '=' -> {
                addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            }
            '<' -> {
                addToken(if (match('=')) LESS_EQUAL else LESS)
            }
            '>' -> {
                addToken(if (match('=')) GREATER_EQUAL else GREATER)
            }
            '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line so skip all remaining characters
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    nesting = 1
                    while (!isAtEnd() && nesting > 0) {
                        when {
                            peek() == '\n' -> {
                                line++
                                advance()
                            }
                            match('*') && peek() == '/' -> {
                                nesting--
                                advance()
                            }
                            match('/') && peek() == '*' -> {
                                nesting++
                                advance()
                            }
                            else -> advance()
                        }
                    }
                    if (nesting > 0) {
                        error(line, "Unclosed block comment")
                    }
                } else {
                    addToken(SLASH)
                }
            }
            // ignore whitespace
            ' ', '\r', '\t' ->  {} // intentionally doing nothing using empty block
            '\n' -> {
                line++
            }

            '"' -> string()
            in '0'..'9' -> number()


            else -> {
                // need to stick the number parsing in the default case due to no function
                // called Char.isDigit (though i might implement it here) since kotlin
                // allows extension functions, nope default 'in' is fine
                // chose to use extension functions in kotlin as it allows function chaining

                when {
                    c.isAlpha() -> identifier()
                    else        -> error(line, "Unexpected character.")
                }

            }

        }
    }

    private fun identifier() {
        while (peek().isAlphaNumeric()) advance()

        val text = source.substring(start, current)
        val type = keywords.getOrDefault(text, IDENTIFIER)
        addToken(type)
    }



    private fun Char.isDigit() = this in '0'..'9'

    private fun number() {
        while (peek().isDigit()) advance()

        // Look for a factional
        if (peek() == '.' && peekNext().isDigit()) {
            // Consume the "."
            advance()


            while (peek().isDigit()) advance()

        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }


    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type,text, literal, line))
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'

        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current+1]
    }

    private fun Char.isAlpha(): Boolean {
        return (this in 'a'..'z') || (this in 'A'..'Z') || this == '_'
    }

    private fun Char.isAlphaNumeric(): Boolean {
        return this.isDigit() || this.isAlpha()
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        advance()
        // Trim's surrounding quotes
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }
}

