package io.joern.jssrc2cpg.passes

import better.files.File
import io.joern.jssrc2cpg.testfixtures.JsSrc2CpgFrontend
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.semanticcpg.language._
import org.scalatest.Inside

class TsAstCreationPassTest extends AbstractPassTest with Inside {

  "AST generation for TS classes" should {

    "have correct structure for simple classes" in AstFixture("""
        |class Greeter {
        |  greeting: string;
        |  greet() {
        |    return "Hello, " + this.greeting;
        |  }
        |}
        |""".stripMargin) { cpg =>
      inside(cpg.typeDecl("Greeter").l) { case List(greeter) =>
        greeter.name shouldBe "Greeter"
        greeter.code shouldBe "class Greeter"
        greeter.fullName shouldBe "code.ts::program:Greeter"
        greeter.filename shouldBe "code.ts"
        greeter.file.name.head shouldBe "code.ts"
        val constructor = greeter.method.name("Greeter<constructor>").head
        greeter.method.isConstructor.head shouldBe constructor
        inside(cpg.typeDecl("Greeter").member.l) { case List(greeting, greet) =>
          greeting.name shouldBe "greeting"
          greeting.code shouldBe "greeting: string;"
          greet.name shouldBe "greet"
          greet.code should (
            startWith("greet() {") and endWith("}")
          )
        }
      }
    }

    "have correct modifier" in AstFixture("""
        |abstract class Greeter {
        |  static a: string;
        |  #b: string;
        |  private c: string;
        |  public d: string;
        |  protected e: string;
        |}
        |""".stripMargin) { cpg =>
      inside(cpg.typeDecl.name("Greeter.*").l) { case List(greeter, greeterMeta) =>
        greeterMeta.name shouldBe "Greeter<meta>"
        greeter.name shouldBe "Greeter"
        cpg.typeDecl.isAbstract.head shouldBe greeter
        greeterMeta.member.isStatic.head shouldBe greeterMeta.member.name("a").head
        greeter.member.isPrivate.l shouldBe List(greeter.member.name("b").head, greeter.member.name("c").head)
        greeter.member.isPublic.head shouldBe greeter.member.name("d").head
        greeter.member.isProtected.head shouldBe greeter.member.name("e").head
      }
    }

    "have correct structure for simple interfaces" in AstFixture("""
        |interface Greeter {
        |  greeting: string;
        |  name?: string;
        |  [propName: string]: any;
        |  "foo": string;
        |  (source: string, subString: string): boolean;
        |}
        |""".stripMargin) { cpg =>
      inside(cpg.typeDecl("Greeter").l) { case List(greeter) =>
        greeter.name shouldBe "Greeter"
        greeter.code shouldBe "interface Greeter"
        greeter.fullName shouldBe "code.ts::program:Greeter"
        greeter.filename shouldBe "code.ts"
        greeter.file.name.head shouldBe "code.ts"
        inside(cpg.typeDecl("Greeter").member.l) { case List(greeting, name, propName, foo, func) =>
          greeting.name shouldBe "greeting"
          greeting.code shouldBe "greeting: string;"
          name.name shouldBe "name"
          name.code shouldBe "name?: string;"
          propName.name shouldBe "propName"
          propName.code shouldBe "[propName: string]: any;"
          foo.name shouldBe "foo"
          foo.code shouldBe "\"foo\": string;"
          func.name shouldBe "anonymous"
          func.code shouldBe "(source: string, subString: string): boolean;"
          func.dynamicTypeHintFullName.head shouldBe "code.ts::program:Greeter:anonymous"
        }
        inside(cpg.typeDecl("Greeter").method.l) { case List(constructor, anon) =>
          constructor.name shouldBe "Greeter<constructor>"
          constructor.fullName shouldBe "code.ts::program:Greeter<constructor>"
          constructor.code shouldBe "new: Greeter"
          greeter.method.isConstructor.head shouldBe constructor
          anon.name shouldBe "anonymous"
          anon.fullName shouldBe "code.ts::program:Greeter:anonymous"
          anon.code shouldBe "(source: string, subString: string): boolean;"
          anon.parameter.name.l shouldBe List("this", "source", "subString")
          anon.parameter.code.l shouldBe List("this", "source: string", "subString: string")
        }
      }
    }

    "have correct structure for interface constructor" in AstFixture("""
       |interface Greeter {
       |  new (param: string) : Greeter
       |}
       |""".stripMargin) { cpg =>
      inside(cpg.typeDecl("Greeter").l) { case List(greeter) =>
        greeter.name shouldBe "Greeter"
        greeter.code shouldBe "interface Greeter"
        greeter.fullName shouldBe "code.ts::program:Greeter"
        greeter.filename shouldBe "code.ts"
        greeter.file.name.head shouldBe "code.ts"
        inside(cpg.typeDecl("Greeter").method.l) { case List(constructor) =>
          constructor.name shouldBe "Greeter<constructor>"
          constructor.fullName shouldBe "code.ts::program:Greeter<constructor>"
          constructor.code shouldBe "new (param: string) : Greeter"
          constructor.parameter.name.l shouldBe List("this", "param")
          constructor.parameter.code.l shouldBe List("this", "param: string")
          greeter.method.isConstructor.head shouldBe constructor
        }
      }
    }

    "have correct structure for simple namespace" in AstFixture("""
       |namespace A {
       |  class Foo {};
       |}
       |""".stripMargin) { cpg =>
      inside(cpg.namespaceBlock("A").l) { case List(a) =>
        a.code should startWith("namespace A")
        a.fullName shouldBe "code.ts::program:A"
        a.typeDecl.name("Foo").head.fullName shouldBe "code.ts::program:A:Foo"
      }
    }

    "have correct structure for nested namespaces" in AstFixture("""
        |namespace A {
        |  namespace B {
        |    namespace C {
        |      class Foo {};
        |    }
        |  }
        |}
        |""".stripMargin) { cpg =>
      inside(cpg.namespaceBlock("A").l) { case List(a) =>
        a.code should startWith("namespace A")
        a.fullName shouldBe "code.ts::program:A"
        a.astChildren.astChildren.isNamespaceBlock.name("B").head shouldBe cpg.namespaceBlock("B").head
      }
      inside(cpg.namespaceBlock("B").l) { case List(b) =>
        b.code should startWith("namespace B")
        b.fullName shouldBe "code.ts::program:A:B"
        b.astChildren.astChildren.isNamespaceBlock.name("C").head shouldBe cpg.namespaceBlock("C").head
      }
      inside(cpg.namespaceBlock("C").l) { case List(c) =>
        c.code should startWith("namespace C")
        c.fullName shouldBe "code.ts::program:A:B:C"
        c.typeDecl.name("Foo").head.fullName shouldBe "code.ts::program:A:B:C:Foo"
      }
    }

    "have correct structure for nested namespaces with path" in AstFixture("""
         |namespace A.B.C {
         |  class Foo {};
         |}
         |""".stripMargin) { cpg =>
      inside(cpg.namespaceBlock("A").l) { case List(a) =>
        a.code should startWith("namespace A")
        a.fullName shouldBe "code.ts::program:A"
        a.astChildren.isNamespaceBlock.name("B").head shouldBe cpg.namespaceBlock("B").head
      }
      inside(cpg.namespaceBlock("B").l) { case List(b) =>
        b.code should startWith("B.C")
        b.fullName shouldBe "code.ts::program:A:B"
        b.astChildren.isNamespaceBlock.name("C").head shouldBe cpg.namespaceBlock("C").head
      }
      inside(cpg.namespaceBlock("C").l) { case List(c) =>
        c.code should startWith("C")
        c.fullName shouldBe "code.ts::program:A:B:C"
        c.typeDecl.name("Foo").head.fullName shouldBe "code.ts::program:A:B:C:Foo"
      }
    }

  }

  private object AstFixture extends Fixture {
    def apply(code: String)(f: Cpg => Unit): Unit = {
      File.usingTemporaryDirectory("jssrc2cpgTest") { dir =>
        val file = dir / "code.ts"
        file.write(code)
        file.deleteOnExit()
        val cpg = new JsSrc2CpgFrontend().execute(dir.toJava)
        f(cpg)
      }
    }
  }

}
