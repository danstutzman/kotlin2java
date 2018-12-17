package com.danstutzman.kotlinc

fun main(args: Array<String>) {
  println("hello")
}

fun convertKotlinFileContentsToJava(className: String, source: String): String {
  val regex = "fun f\\((.*?)\\) = ([0-9a-z +]+)$".toRegex()
  val matchResult = regex.find(source) ?:
    throw RuntimeException("Couldn't parse source '${source}'")
  val (params, exprSource) = matchResult.destructured
  val paramNames = parseParamNames(params)
  val returnExprAst = Parser(exprSource.toCharArray()).expr()
  val methodAst = Ast.Method(paramNames, returnExprAst)
  return "public final class ${className} {\n" +
    convertMethodAstToJava(methodAst) +
    "}\n"
}

fun parseParamNames(paramNames: String): List<String> {
  if (paramNames == "") {
    return listOf<String>()
  } else {
    val regex = "([a-z]+): Int".toRegex()
    val matchResult = regex.find(paramNames) ?:
      throw RuntimeException("Couldn't parse paramNames '${paramNames}'")
    val (paramName) = matchResult.destructured
    return listOf(paramName)
  }
}

fun zipParamNamesToArgs(paramNames: List<String>, args: List<Int>):
  Map<String, Int> {
  val map = mutableMapOf<String, Int>()
  for (i in 0..paramNames.size - 1) {
    map[paramNames[i]] = args[i]
  }
  return map
}

class Parser(
  var chars: CharArray
) {
  var i = 0

  fun expr(): Ast {
    spaces()
    var exprSoFar: Ast = numLiteralOrVar()
    while (i < chars.size) {
      spaces()
      if (i == chars.size) {
        break
      }
      plus()
      spaces()
      exprSoFar = Ast.Plus(exprSoFar, numLiteralOrVar())
    }
    return exprSoFar
  }

  fun spaces() {
    while (i < chars.size && chars[i] == ' ') {
      println("' '")
      i += 1
    }
  }

  fun plus() {
    if (i >= chars.size) {
      throw RuntimeException("Expected '+' but got EOF")
    }
    if (chars[i] != '+') {
      throw RuntimeException("Expected '+' but got '${chars[i]}'")
    }
    println("'+'")
    i += 1
  }

  fun numLiteralOrVar(): Ast {
    val s = StringBuilder()
    if (chars[i] >= '0' && chars[i] <= '9') {
      while (i < chars.size && chars[i] >= '0' && chars[i] <= '9') {
        s.append(chars[i])
        println("'${chars[i]}'")
        i += 1
      }
      return Ast.NumLiteral(s.toString().toInt())
    } else if (chars[i] >= 'a' && chars[i] <= 'z') {
      while (i < chars.size && chars[i] >= 'a' && chars[i] <= 'z') {
        s.append(chars[i])
        println("'${chars[i]}'")
        i += 1
      }
      return Ast.Var(s.toString())
    } else {
      throw RuntimeException("Expected alphanumeric but got '${chars[i]}'")
    }
  }
}

fun convertMethodAstToJava(method: Ast.Method): String =
  "public static final int f(" +
  method.paramNames.map { "int ${it}" }.joinToString(", ") +
  ") {\n" +
  "return (${convertExprAstToJava(method.returnExpr)});\n" +
  "}\n"

fun convertExprAstToJava(expr: Ast): String =
  when (expr) {
    is Ast.Method -> throw RuntimeException("Method not allowed here")
    is Ast.Plus ->
      "((${convertExprAstToJava(expr.node1)})+" +
      "(${convertExprAstToJava(expr.node2)}))"
    is Ast.NumLiteral -> expr.int.toString()
    is Ast.Var -> expr.name
  }
