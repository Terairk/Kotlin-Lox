package org.terairk.lox

class LoxInstance(private val klass: LoxClass) {
    private val fields = HashMap<String, Any?>()
    override fun toString(): String {
        return klass.name + " instance"
    }

    operator fun get(name: Token): Any? {
        if (name.lexeme in fields.keys) {
            return fields[name.lexeme]
        }

        val method = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}