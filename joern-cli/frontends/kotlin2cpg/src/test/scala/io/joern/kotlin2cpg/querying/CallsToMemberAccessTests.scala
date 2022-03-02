package io.joern.kotlin2cpg.querying

import io.joern.kotlin2cpg.Kt2CpgTestContext
import io.shiftleft.codepropertygraph.generated.nodes.Call
import io.shiftleft.semanticcpg.language._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CallsToMemberAccessTests extends AnyFreeSpec with Matchers {
  "CPG for code with class method referencing member in a call" - {
    lazy val cpg = Kt2CpgTestContext.buildCpg("""
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

    "should contain a CALL node for the referenced member" in {
      val List(firstArg) = cpg.call.codeExact("println(x)").argument.l
      firstArg.code shouldBe "x"
      firstArg shouldBe a [Call]
    }
  }

}
