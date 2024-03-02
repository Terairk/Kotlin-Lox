package org.terairk.lox

// disables some JVM machinery we don't need
class Return(val value: Any?): RuntimeException(null, null, false, false) {

}