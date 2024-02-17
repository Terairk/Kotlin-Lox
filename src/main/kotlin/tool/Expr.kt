package com.terairk.lox

import org.terairk.lox.Token

abstract class Expr {
  class Binary(val left: Expr, val operator: Token, val right: Expr) {
  }
  class Grouping(val expression: Expr) {
  }
  class Literal(val value: Any?) {
  }
  class Unary(val operator: Token, val right: Expr) {
  }
}
