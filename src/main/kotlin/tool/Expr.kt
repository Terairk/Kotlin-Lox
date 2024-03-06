package org.terairk.lox

import org.terairk.lox.Token

abstract class Expr {
  interface Visitor<T> {
    fun visitAssignExpr(expr: Assign): T
    fun visitBinaryExpr(expr: Binary): T
    fun visitCallExpr(expr: Call): T
    fun visitGetExpr(expr: Get): T
    fun visitSetExpr(expr: Set): T
    fun visitThisExpr(expr: This): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitLogicalExpr(expr: Logical): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVariableExpr(expr: Variable): T
  }
  class Assign (val name: Token, val value: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitAssignExpr(this)
    }
  }
  class Binary (val left: Expr, val operator: Token, val right: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitBinaryExpr(this)
    }
  }
  class Call (val callee: Expr, val paren: Token, val arguments: List<Expr>): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitCallExpr(this)
    }
  }
  class Get (val instance: Expr, val name: Token): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitGetExpr(this)
    }
  }
  class Set (val instance: Expr, val name: Token, val value: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitSetExpr(this)
    }
  }
  class This (val keyword: Token): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitThisExpr(this)
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
  class Logical (val left: Expr, val operator: Token, val right: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitLogicalExpr(this)
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
