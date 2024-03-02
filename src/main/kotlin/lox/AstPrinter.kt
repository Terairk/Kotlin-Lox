//package lox
//
//import org.terairk.lox.Expr
//import org.terairk.lox.Token
//import org.terairk.lox.TokenType
//
//class AstPrinter : Expr.Visitor<String> {
//    fun print(expr: Expr): String {
//        return expr.accept(this)
//    }
//
//    override fun visitBinaryExpr(expr: Expr.Binary): String {
//        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
//    }
//
//    override fun visitGroupingExpr(expr: Expr.Grouping): String {
//        return parenthesize("group", expr.expression)
//    }
//
//    override fun visitLiteralExpr(expr: Expr.Literal): String {
//        return expr.value?.toString() ?: "nil"
//    }
//
//    override fun visitUnaryExpr(expr: Expr.Unary): String {
//        return parenthesize(expr.operator.lexeme, expr.right)
//    }
//
//    // temporary fixes
//    override fun visitVariableExpr(expr: Expr.Variable): String {
//        return "variable"
//    }
//
//    // temporary fixes
//    override fun visitAssignExpr(expr: Expr.Assign): String {
//        return "assign"
//    }
//
//    private fun parenthesize(
//        name: String,
//        vararg exprs: Expr,
//    ): String {
//        val builder = StringBuilder()
//        builder.append("(").append(name)
//        for (expr: Expr in exprs) {
//            builder.append(" ")
//            builder.append(expr.accept(this))
//        }
//        builder.append(")")
//        return builder.toString()
//    }
//}
//
//fun main() {
//    val expression =
//        Expr.Binary(
//            Expr.Unary(
//                Token(TokenType.MINUS, "-", null, 1),
//                Expr.Literal(123),
//            ),
//            Token(TokenType.STAR, "*", null, 1),
//            Expr.Grouping(
//                Expr.Literal(45.67),
//            ),
//        )
//
//    println(AstPrinter().print(expression))
//}
