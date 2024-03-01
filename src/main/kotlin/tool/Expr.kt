package org.terairk.lox

import org.terairk.lox.Token

abstract class Expr {
  interface Visitor<T> {
    fun visitBinaryExpr(expr: Binary): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVariableExpr(expr: Variable): T
  }
  class Binary (val left: Expr, val operator: Token, val right: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitBinaryExpr(this)
    }
  }
  class Grouping (val expression: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitGroupingExpr(this)
    }
  }
  class Literal (val value: Any?): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitLiteralExpr(this)
    }
  }
  class Unary (val operator: Token, val right: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitUnaryExpr(this)
    }
  }
  class Variable (val name: Token): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitVariableExpr(this)
    }
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}
