package org.terairk.lox

import org.terairk.lox.Token

// this is super concise in Kotlin
class RuntimeError(val token: Token, override val message: String): RuntimeException(message) {
}