package com.danstutzman.kotlinc

import java.io.File
import kotlin.test.assertEquals
import org.junit.Test

fun compare(
    className: String,
    kotlin: String,
    methodName: String,
    paramTypes: Array<Class<*>>,
    args: Array<Any>,
    expectedOutput: Any) {
  val java = convertKotlinFileContentsToJava(className, kotlin)
  val javaFile = File("src/test/fixtures/${className}.java")
  if (!javaFile.exists()) {
    javaFile.writeText(java)
  }
  assertEquals(javaFile.readText(), java)

  val output = Class
    .forName(className)
    .getMethod(methodName, *paramTypes)
    .invoke(null, *args)
  assertEquals(expectedOutput, output)
}

val NO_PARAMS = arrayOf<Class<*>>()
val NO_ARGS = arrayOf<Any>()

class TestKotlinToJava {
  @Test fun test1() =
    compare("F1Kt", "fun f() = 1", "f", NO_PARAMS, NO_ARGS, 1)
  @Test fun test2() =
    compare("F2Kt", "fun f() = 2", "f", NO_PARAMS, NO_ARGS, 2)
  @Test fun test1Plus1() =
    compare("F1Plus1Kt", "fun f() = 1 + 1", "f", NO_PARAMS, NO_ARGS, 2)
  @Test fun testX() =
    compare("FX", "fun f(x: Int) = x",
      "f", arrayOf(Int::class.java), arrayOf(3), 3)
}
