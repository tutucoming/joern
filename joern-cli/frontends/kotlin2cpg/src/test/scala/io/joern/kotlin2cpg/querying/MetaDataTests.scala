package io.joern.kotlin2cpg.querying

import io.joern.kotlin2cpg.TestContext
import io.shiftleft.semanticcpg.language._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MetaDataTests extends AnyFreeSpec with Matchers {

  lazy val cpg = TestContext.buildCpg("""
      |class ClassFoo {}
      |""".stripMargin)

  "should contain exactly one META_DATA node with all mandatory fields set" in {
    val List(md) = cpg.metaData.l
    md.language shouldBe "KOTLIN"
    md.version shouldBe "0.1"
    md.overlays shouldBe List("base", "controlflow", "typerel", "callgraph")
  }

  "should not have any incoming or outgoing edges" in {
    cpg.metaData.size shouldBe 1
    cpg.metaData.in.l shouldBe List()
    cpg.metaData.out.l shouldBe List()
  }
}
