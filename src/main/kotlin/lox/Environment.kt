package org.terairk.lox

// we have a parent-pointer tree!
class Environment(val enclosing: Environment? = null) {
    private val values = HashMap<String, Any?>()

    fun define(
        name: String,
        value: Any?,
    ) {
        values[name] = value
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }
    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }

        return environment
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

        println("Offender is ${name.lexeme}")

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
