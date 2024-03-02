package org.terairk.lox

// this is super concise in Kotlin
class RuntimeError(val token: Token, override val message: String) : RuntimeException(message)
