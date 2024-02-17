package tool

import java.io.PrintWriter
import java.util.Arrays
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }
    val outputDir = args[0]
    // my own notes, seems this AST builder could be done using kotlin's
    // custom DSL syntax, I might try to do this later on
    defineAst(outputDir, "Expr", listOf(
        "Binary   ; val left: Expr, val operator: Token, val right: Expr",
        "Grouping ; val expression: Expr",
        "Literal  ; val value: Any?",
        "Unary    ; val operator: Token, val right: Expr",
    )
    )
}

private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("package com.terairk.lox")
    writer.println()
    writer.println("import org.terairk.lox.Token")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)

    // The AST Classes

    for (type: String in types) {
        val className = type.split(";")[0].trim()
        val fields = type.split(";")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.println("}")
    writer.close()
}

// here we might be able to leverage Functional interfaces in kotlin
// aka SAM (Single Abstract Method) to implement the visitor pattern
// https://kotlinlang.org/docs/fun-interfaces.html
private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {

}

// surprisingly the kotlin code for this is way simpler if I construct my strings well
// could make a converter that converts java syntax to kotlin
// no need to do a separate constructor, or storing parameters in fields
// or even declaring the fields
private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    writer.println("  class $className($fieldList) {")
    writer.println("  }")
}