package io.joern.kotlin2cpg.querying

import io.joern.kotlin2cpg.TestContext
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.semanticcpg.language._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ArithmeticOperationsTests extends AnyFreeSpec with Matchers {

  "CPG for code with simple arithmetic operations" - {

    lazy val cpg = TestContext.buildCpg("""
        |fun main(args : Array<String>) {
        |  println(1 + 2)
        |  println(1 - 2)
        |  println(1 * 2)
        |  println(1 / 2)
        |}
        |""".stripMargin)

    "should contain a CALL node for the addition op with correct props set" in {
      val List(p) = cpg.call.methodFullName(Operators.addition).l
      p.argument.size shouldBe 2
      p.lineNumber shouldBe Some(3)
      p.code shouldBe "1 + 2"
      p.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    }

    "should contain a CALL node for the subtraction op with correct props set" in {
      val List(p) = cpg.call.methodFullName(Operators.subtraction).l
      p.argument.size shouldBe 2
      p.lineNumber shouldBe Some(4)
      p.code shouldBe "1 - 2"
      p.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    }

    "should contain a CALL node for the multiplication op with correct props set" in {
      val List(p) = cpg.call.methodFullName(Operators.multiplication).l
      p.argument.size shouldBe 2
      p.lineNumber shouldBe Some(5)
      p.code shouldBe "1 * 2"
      p.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    }

    "should contain a CALL node for the division op with correct props set" in {
      val List(p) = cpg.call.methodFullName(Operators.division).l
      p.argument.size shouldBe 2
      p.lineNumber shouldBe Some(6)
      p.code shouldBe "1 / 2"
      p.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    }
  }
}
