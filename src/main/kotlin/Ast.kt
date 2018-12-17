package com.danstutzman.kotlinc

sealed class Ast {
  data class Method(
    val paramNames: List<String>,
    val returnExpr: Ast): Ast()
  data class Plus(val node1: Ast, val node2: Ast): Ast()
  data class NumLiteral(val int: Int): Ast()
  data class Var(val name: String): Ast()
}
