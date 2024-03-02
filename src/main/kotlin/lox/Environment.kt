package org.terairk.lox

// we have a parent-pointer tree!
class Environment(private val enclosing: Environment? = null) {
    private val values = HashMap<String, Any?>()

    fun define(
        name: String,
        value: Any?,
    ) {
        values[name] = value
    }

    // simplified this code down with Kotlin / well more concise
    operator fun get(name: Token): Any {
        if (name.lexeme in values.keys) {
            // we know it exists
            return values[name.lexeme]!!
        }

        if (enclosing != null) {
            return enclosing[name]
        }

        throw RuntimeError(
            name,
            "Undefined variable '${name.lexeme}'.",
        )
    }

    fun assign(
        name: Token,
        value: Any?,
    ) {
        if (name.lexeme in values.keys) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(
            name,
            "Undefined variable '${name.lexeme}'.",
        )
    }
}
