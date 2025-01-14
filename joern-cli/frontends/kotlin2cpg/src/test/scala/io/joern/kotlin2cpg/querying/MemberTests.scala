package io.joern.kotlin2cpg.querying

import io.joern.kotlin2cpg.TestContext
import io.shiftleft.semanticcpg.language._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MemberTests extends AnyFreeSpec with Matchers {

  "CPG for code with simple member" - {
    lazy val cpg = TestContext.buildCpg("""
        |class Foo {
        |  val bar: Int = 1
        |}
        |""".stripMargin)

    "should contain a MEMBER node with the correct properties set" in {
      val List(m) = cpg.member("bar").l
      m.name shouldBe "bar"
      m.code shouldBe "bar"
      m.typeFullName shouldBe "int"
      m.lineNumber shouldBe Some(3)
      m.columnNumber shouldBe Some(6)
      m.order shouldBe 2
    }

    "should allow traversing from MEMBER to TYPE_DECL" in {
      val List(td) = cpg.member.typeDecl.l
      td.name shouldBe "Foo"
    }
  }

  "CPG for code with member defined in primary constructor" - {
    lazy val cpg = TestContext.buildCpg("""
      |package mypkg
      |
      |class AClass(private val x: String) {
      |    fun printX() {
      |        println(x)
      |    }
      |}
      |
      |fun main() {
      |    val a = AClass("A_MESSAGE")
      |    a.printX()
      |}
      |""".stripMargin)

    "should contain a MEMBER node with the correct properties set" in {
      val List(m) = cpg.member("x").l
      m.code shouldBe "x"
      m.typeFullName shouldBe "java.lang.String"
    }
  }

  "CPG for code with member which gets its init from constructor arg" - {
    lazy val cpg = TestContext.buildCpg("""
        |class Foo(p: String) {
        |  val bar: Int = p
        |}
        |""".stripMargin)

    "should contain a MEMBER node with the correct properties set" in {
      val List(m) = cpg.member("bar").l
      m.name shouldBe "bar"
      m.code shouldBe "bar"
      m.typeFullName shouldBe "int"
      m.lineNumber shouldBe Some(3)
      m.columnNumber shouldBe Some(6)
      m.order shouldBe 2
    }

    "should allow traversing from MEMBER to TYPE_DECL" in {
      val List(td) = cpg.member.typeDecl.l
      td.name shouldBe "Foo"
    }

    // TODO: test lowering of setting member to expression
  }

  "CPG for code with member which gets its init from constructor arg with expression" - {
    lazy val cpg = TestContext.buildCpg("""
        |class Foo(p: String) {
        |  val bar: Int = p + "baz"
        |}
        |""".stripMargin)

    "should contain a MEMBER node with the correct properties set" in {
      val List(m) = cpg.member("bar").l
      m.name shouldBe "bar"
      m.code shouldBe "bar"
      m.typeFullName shouldBe "int"
      m.lineNumber shouldBe Some(3)
      m.columnNumber shouldBe Some(6)
      m.order shouldBe 2
    }

    "should allow traversing from MEMBER to TYPE_DECL" in {
      val List(td) = cpg.member.typeDecl.l
      td.name shouldBe "Foo"
    }
  }

}
