package org.terairk.lox

import org.terairk.lox.Token

abstract class Stmt {
  interface Visitor<T> {
    fun visitExpressionStmt(stmt: Expression): T
    fun visitPrintStmt(stmt: Print): T
    fun visitVarStmt(stmt: Var): T
  }
  class Expression (val expression: Expr): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitExpressionStmt(this)
    }
  }
  class Print (val expression: Expr): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitPrintStmt(this)
    }
  }
  class Var (val name: Token, val initializer: Expr?): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitVarStmt(this)
    }
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}
