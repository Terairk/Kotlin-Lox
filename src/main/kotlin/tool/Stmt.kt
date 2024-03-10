package org.terairk.lox

import org.terairk.lox.Token

abstract class Stmt {
  interface Visitor<T> {
    fun visitBlockStmt(stmt: Block): T
    fun visitClassStmt(stmt: Class): T
    fun visitExpressionStmt(stmt: Expression): T
    fun visitFunctionStmt(stmt: Function): T
    fun visitPrintStmt(stmt: Print): T
    fun visitVarStmt(stmt: Var): T
    fun visitReturnStmt(stmt: Return): T
    fun visitIfStmt(stmt: If): T
    fun visitWhileStmt(stmt: While): T
  }
  class Block (val statements: List<Stmt>): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitBlockStmt(this)
    }
  }
  class Class (val name: Token, val superclass: Expr.Variable?, val methods: List<Stmt.Function>): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitClassStmt(this)
    }
  }
  class Expression (val expression: Expr): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitExpressionStmt(this)
    }
  }
  class Function (val name: Token, val params: List<Token>, val body: List<Stmt>): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitFunctionStmt(this)
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
  class Return (val keyword: Token, val value: Expr?): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitReturnStmt(this)
    }
  }
  class If (val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitIfStmt(this)
    }
  }
  class While (val condition: Expr, val body: Stmt): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitWhileStmt(this)
    }
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}
