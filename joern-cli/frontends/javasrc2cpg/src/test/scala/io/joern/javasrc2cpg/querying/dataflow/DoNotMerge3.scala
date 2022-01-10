package io.joern.javasrc2cpg.querying.dataflow

import io.joern.dataflowengineoss.language._
import io.joern.javasrc2cpg.testfixtures.JavaDataflowFixture
import io.shiftleft.codepropertygraph.generated.nodes.Call
import io.shiftleft.semanticcpg.language._

class DoNotMerge3 extends JavaDataflowFixture {

  behavior of "Dataflow through objects"

  override val code: String =
    """
      |class Bar {
      |  String value;
      |
      |  public Bar init(String s) {
      |    value = s;
      |    return this;
      |  }
      |
      |  public static void sink(Bar b) {}
      |
      |  public static String source() { return "MALICIOUS"; }
      |
      |  public static void test1(Bar b) {
      |    // Triggers the bug
      |    b.init("MALICIOUS");
      |    sink(b);
      |  }
      |
      |  public static void test2(Bar b) {
      |    // Does not trigger the bug
      |    String s = "MALICIOUS";
      |    b.init(s);
      |    sink(b);
      |  }
      |
      |  public static void test34(Bar b) {
      |    // Triggers bug if `source()` is the source.
      |    // Does not trigger bug if "MALICIOUS" in `source` is the source.
      |    b.init(source());
      |    sink(b);
      |  }
      |}
      |""".stripMargin

  it should "find consistent results for reachableBy and reachableBySource in test1" in {
    def source = cpg.method.name("test1").literal.code("\"MALICIOUS\"")
    def sink = cpg.method.name("sink").parameter

    sink.reachableBy(source).size shouldBe 1
    sink.reachableByFlows(source).size shouldBe 1
  }

  it should "find consistent results for reachableBy and reachableBySource in test2" in {
    def source = cpg.method.name("test2").literal.code("\"MALICIOUS\"")
    def sink = cpg.method.name("sink").parameter

    sink.reachableBy(source).size shouldBe 1
    sink.reachableByFlows(source).size shouldBe 1
  }

  it should "find consistent results for reachableBy and reachableBySource in test3" in {
    def source = cpg.method.name("test34").call.name("source")
    def sink = cpg.method.name("sink").parameter

    sink.reachableBy(source).size shouldBe 1
    sink.reachableByFlows(source).size shouldBe 1
  }

  it should "find consistent results for reachableBy and reachableBySource in test4" in {
    def source = cpg.method.name("source").literal.code("\"MALICIOUS\"")
    def sink = cpg.method.name("sink").parameter

    sink.reachableBy(source).size shouldBe 1
    sink.reachableByFlows(source).size shouldBe 1
  }
}
